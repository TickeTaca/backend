package com.ticketaca.auth.dto.response;

public record EmailVerificationResponse(
        String email,
        boolean verified
) {
}
