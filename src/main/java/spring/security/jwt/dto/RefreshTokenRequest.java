package spring.security.jwt.dto;

public record RefreshTokenRequest(
        String refreshToken
) {
}
