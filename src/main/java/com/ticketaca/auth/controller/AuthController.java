package com.ticketaca.auth.controller;

import com.ticketaca.auth.dto.request.EmailLoginRequest;
import com.ticketaca.auth.dto.request.EmailSignupRequest;
import com.ticketaca.auth.dto.request.EmailVerificationRequest;
import com.ticketaca.auth.dto.request.EmailVerificationResendRequest;
import com.ticketaca.auth.dto.request.KakaoLoginRequest;
import com.ticketaca.auth.dto.request.LogoutRequest;
import com.ticketaca.auth.dto.request.TokenRefreshRequest;
import com.ticketaca.auth.dto.response.EmailSignupResponse;
import com.ticketaca.auth.dto.response.EmailVerificationResponse;
import com.ticketaca.auth.dto.response.KakaoLoginEntrypointResponse;
import com.ticketaca.auth.dto.response.TokenResponse;
import com.ticketaca.auth.service.AuthService;
import com.ticketaca.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/email/signup")
    public ResponseEntity<ApiResponse<EmailSignupResponse>> signup(
            @Valid @RequestBody EmailSignupRequest request,
            HttpServletRequest httpServletRequest
    ) {
        EmailSignupResponse response = authService.signup(request, extractClientIp(httpServletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyEmail(request)));
    }

    @PostMapping("/email/verification/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(
            @Valid @RequestBody EmailVerificationResendRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authService.resendVerificationCode(request, extractClientIp(httpServletRequest));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/email/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody EmailLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/social/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginEntrypointResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.kakaoEntrypoint(request)));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }
}
