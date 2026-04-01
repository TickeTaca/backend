package com.ticketaca.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        Jwt jwt,
        EmailVerification emailVerification
) {
    public record Jwt(
            String issuer,
            String secret,
            long accessTokenExpiresInSeconds,
            long refreshTokenExpiresInSeconds
    ) {
    }

    public record EmailVerification(
            long expiresInMinutes
    ) {
    }
}
