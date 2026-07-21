package ec.edu.ups.icc.fundamentos01.security.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ec.edu.ups.icc.fundamentos01.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RefreshTokenRequestDto;
import ec.edu.ups.icc.fundamentos01.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.fundamentos01.security.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/*
 * Controlador REST para autenticación.
 *
 * Estos endpoints son públicos: register, login, refresh, logout.
 * Por eso NO se usa @SecurityRequirement a nivel de clase: ninguno
 * de ellos exige un access token en el header Authorization.
 */
@Tag(
        name = "Autenticación",
        description = "Endpoints públicos para registro, inicio de sesión y renovación de tokens"
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida credenciales y devuelve un access token y un refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Crea un nuevo usuario, asigna ROLE_USER y devuelve un access token y un refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @Operation(
            summary = "Renovar tokens",
            description = """
                    Recibe un refresh token válido y devuelve un par nuevo de tokens
                    (access + refresh), revocando el refresh token usado (rotación).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados correctamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado o revocado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Revoca el refresh token recibido, invalidándolo para futuras renovaciones."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado o revocado")
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
    }
}