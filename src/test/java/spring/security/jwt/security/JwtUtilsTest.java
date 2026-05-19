package spring.security.jwt.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {
    private static final String TEST_SECRET = "1234567890123456789012345678901234567890";
    private static final String TEST_ISSUER = "test-issuer";
    private static final String TEST_AUDIENCE = "test-audience";

    /**
     * Verifies valid token is accepted and user can be extracted.
     */
    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        JwtUtils jwtUtils = createJwtUtils(TEST_SECRET, TEST_ISSUER, TEST_AUDIENCE, 3600L, 86400L);

        String token = jwtUtils.generateAccessToken("test-user");

        assertTrue(jwtUtils.validateAccessToken(token));
        assertEquals("test-user", jwtUtils.getUserFromToken(token));
    }

    /**
     * Verifies tampered token is rejected.
     */
    @Test
    void validateTokenShouldReturnFalseForTamperedToken() {
        JwtUtils jwtUtils = createJwtUtils(TEST_SECRET, TEST_ISSUER, TEST_AUDIENCE, 3600L, 86400L);
        String token = jwtUtils.generateAccessToken("test-user");
        String tamperedToken = tamperJwtPayload(token);

        assertFalse(jwtUtils.validateAccessToken(tamperedToken));
    }

    /**
     * Verifies malformed token is rejected.
     */
    @Test
    void validateTokenShouldReturnFalseForMalformedToken() {
        JwtUtils jwtUtils = createJwtUtils(TEST_SECRET, TEST_ISSUER, TEST_AUDIENCE, 3600L, 86400L);

        assertFalse(jwtUtils.validateAccessToken("not-a-jwt-token"));
    }

    /**
     * Verifies refresh token validation logic accepts refresh token and rejects it as access token.
     */
    @Test
    void validateRefreshTokenShouldRespectTokenType() {
        JwtUtils jwtUtils = createJwtUtils(TEST_SECRET, TEST_ISSUER, TEST_AUDIENCE, 3600L, 86400L);
        String refreshToken = jwtUtils.generateRefreshToken("test-user");

        assertTrue(jwtUtils.validateRefreshToken(refreshToken));
        assertFalse(jwtUtils.validateAccessToken(refreshToken));
    }

    /**
     * Builds JwtUtils instance with configured JWT properties for testing.
     */
    private JwtUtils createJwtUtils(
            String secret,
            String issuer,
            String audience,
            long accessExpirationSeconds,
            long refreshExpirationSeconds
    ) {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtIssuer", issuer);
        ReflectionTestUtils.setField(jwtUtils, "jwtAudience", audience);
        ReflectionTestUtils.setField(jwtUtils, "accessExpirationSeconds", accessExpirationSeconds);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpirationSeconds", refreshExpirationSeconds);
        ReflectionTestUtils.invokeMethod(jwtUtils, "init");
        return jwtUtils;
    }

    private String tamperJwtPayload(String token) {
        String[] parts = token.split("\\.");
        String payload = parts[1];
        int indexToFlip = payload.length() / 2;
        char originalChar = payload.charAt(indexToFlip);
        char replacementChar = originalChar == 'a' ? 'b' : 'a';
        String tamperedPayload =
                payload.substring(0, indexToFlip) + replacementChar + payload.substring(indexToFlip + 1);
        return parts[0] + "." + tamperedPayload + "." + parts[2];
    }
}
