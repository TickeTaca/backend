package com.ticketaca.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationResendRequest(
        @NotBlank
        @Email
        String email
) {
}
