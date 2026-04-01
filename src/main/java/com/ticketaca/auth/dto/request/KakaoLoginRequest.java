package com.ticketaca.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KakaoLoginRequest(
        @NotBlank
        @Size(max = 500)
        String authorizationCode,
        @NotBlank
        @Size(max = 500)
        String redirectUri
) {
}
