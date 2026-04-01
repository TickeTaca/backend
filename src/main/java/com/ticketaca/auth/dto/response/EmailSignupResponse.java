package com.ticketaca.auth.dto.response;

public record EmailSignupResponse(
        Long memberId,
        String email,
        boolean emailVerified
) {
}
