package spring.security.jwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    @Value("${jwt.access-expiration-seconds}")
    private long accessExpirationSeconds;

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirationSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username) {
        return generateToken(username, ACCESS_TOKEN_TYPE, accessExpirationSeconds);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, REFRESH_TOKEN_TYPE, refreshExpirationSeconds);
    }

    public String generateToken(String username) {
        return generateAccessToken(username);
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    public String getUserFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        return validateAccessToken(token);
    }

    public boolean validateAccessToken(String token) {
        return validateTokenByType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenByType(token, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(String username, String tokenType, long expirationSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (expirationSeconds * 1000));
        return Jwts.builder()
                .subject(username)
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean validateTokenByType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return hasExpectedClaims(claims, expectedType);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT Validation error : {}", e.getMessage());
        }
        return false;
    }

    private boolean hasExpectedClaims(Claims claims, String expectedType) {
        Set<String> audiences = claims.getAudience();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return jwtIssuer.equals(claims.getIssuer())
                && claims.getId() != null
                && tokenType != null
                && expectedType.equals(tokenType)
                && audiences != null
                && audiences.contains(jwtAudience);
    }
}
