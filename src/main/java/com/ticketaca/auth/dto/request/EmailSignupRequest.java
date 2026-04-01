package com.ticketaca.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailSignupRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,64}$",
                message = "비밀번호는 8~64자, 영문/숫자/특수문자를 포함해야 합니다."
        )
        String password,
        @NotBlank
        @Size(max = 100)
        String name
) {
}
