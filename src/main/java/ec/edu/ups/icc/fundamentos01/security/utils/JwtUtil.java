package ec.edu.ups.icc.fundamentos01.security.utils;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.fundamentos01.security.config.JwtProperties;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /*
     * Genera un access token desde Authentication (se usa en login).
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return buildToken(userPrincipal, jwtProperties.getExpiration(), ACCESS_TOKEN_TYPE);
    }

    /*
     * Genera un access token desde UserDetailsImpl (se usa en register y refresh).
     */
    public String generateAccessTokenFromUserDetails(UserDetailsImpl userDetails) {
        return buildToken(userDetails, jwtProperties.getExpiration(), ACCESS_TOKEN_TYPE);
    }

    /*
     * Genera un refresh token. Dura más tiempo y solo debe usarse en
     * POST /api/auth/refresh.
     */
    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return buildToken(userDetails, jwtProperties.getRefreshExpiration(), REFRESH_TOKEN_TYPE);
    }

    private String buildToken(UserDetailsImpl userDetails, Long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("email", userDetails.getEmail())
                .claim("name", userDetails.getName())
                .claim("roles", roles)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /*
     * Extrae el tipo del token: "access" o "refresh".
     */
    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /*
     * Valida firma, formato y expiración. No valida el tipo.
     */
    public boolean validateToken(String authToken) {
        try {
            getClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Firma JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string está vacío: {}", ex.getMessage());
        }
        return false;
    }

    /*
     * Válido SOLO si es un access token. Se usa en JwtAuthenticationFilter.
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    /*
     * Válido SOLO si es un refresh token. Se usa en /auth/refresh.
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token) && REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}