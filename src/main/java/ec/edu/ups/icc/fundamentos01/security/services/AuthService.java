package ec.edu.ups.icc.fundamentos01.security.services;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /*
     * Login:
     * 1. Valida credenciales.
     * 2. Genera access token.
     * 3. Revoca refresh tokens anteriores (una sola sesión activa).
     * 4. Genera un refresh token nuevo.
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtil.generateAccessToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        UserEntity user = findActiveUserById(userDetails.getId());

        refreshTokenService.revokeAllByUser(user);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user, userDetails);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    /*
     * Registro: crea el usuario con ROLE_USER, genera access token y
     * refresh token directamente (queda logueado sin login aparte).
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        UserEntity user = new UserEntity();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        RoleEntity userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Rol por defecto no encontrado"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        user = userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user, userDetails);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    /*
     * Refresh: valida el refresh token, lo revoca (rotación) y genera
     * un par nuevo de tokens.
     */
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {

        RefreshTokenEntity currentRefreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        UserEntity user = currentRefreshToken.getUser();

        refreshTokenService.revoke(currentRefreshToken);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String newAccessToken = jwtUtil.generateAccessTokenFromUserDetails(userDetails);
        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user, userDetails);

        return buildAuthResponse(newAccessToken, newRefreshToken.getToken(), user);
    }

    /*
     * Logout: revoca el refresh token recibido.
     */
    @Transactional
    public void logout(RefreshTokenRequestDto request) {
        RefreshTokenEntity refreshToken =
                refreshTokenService.validateAndGetActiveToken(request.getRefreshToken());

        refreshTokenService.revoke(refreshToken);
    }

    private UserEntity findActiveUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BadRequestException("Usuario no válido"));
    }

    private AuthResponseDto buildAuthResponse(String accessToken, String refreshToken, UserEntity user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }
}