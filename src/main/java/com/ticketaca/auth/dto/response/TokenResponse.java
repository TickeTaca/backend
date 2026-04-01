package com.ticketaca.auth.dto.response;

public record TokenResponse(
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
