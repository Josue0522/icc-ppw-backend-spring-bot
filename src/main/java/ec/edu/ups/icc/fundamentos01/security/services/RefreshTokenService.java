package ec.edu.ups.icc.fundamentos01.security.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.fundamentos01.security.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.fundamentos01.security.utils.JwtUtil;
import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;

/*
 * Servicio encargado de crear, validar, rotar y revocar refresh tokens.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    /*
     * Crea un refresh token: lo firma como JWT y lo guarda en base de datos.
     */
    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user, UserDetailsImpl userDetails) {
        String token = jwtUtil.generateRefreshToken(userDetails);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getRefreshExpiration()));

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(user, token, expiresAt);

        return refreshTokenRepository.save(refreshToken);
    }

    /*
     * Valida un refresh token recibido desde el cliente:
     * 1. Firma válida y tipo correcto (refresh).
     * 2. Existe en base de datos y no está revocado.
     * 3. No está expirado.
     * 4. El usuario dueño sigue activo.
     */
    @Transactional
    public RefreshTokenEntity validateAndGetActiveToken(String token) {

        if (!jwtUtil.validateRefreshToken(token)) {
            throw new BadRequestException("Refresh token inválido");
        }

        RefreshTokenEntity refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new BadRequestException("Refresh token no encontrado o revocado"));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BadRequestException("Refresh token expirado");
        }

        if (refreshToken.getUser() == null || refreshToken.getUser().isDeleted()) {
            throw new BadRequestException("Usuario no válido para este refresh token");
        }

        return refreshToken;
    }

    /*
     * Revoca un refresh token específico. Se usa en refresh (rotación) y logout.
     */
    @Transactional
    public void revoke(RefreshTokenEntity refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /*
     * Revoca todos los refresh tokens activos de un usuario.
     * Se usa en login para dejar una sola sesión activa por usuario.
     */
    @Transactional
    public void revokeAllByUser(UserEntity user) {
        List<RefreshTokenEntity> tokens = refreshTokenRepository
                .findByUserIdAndRevokedFalse(user.getId());

        tokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }
}