package com.ticketaca.auth.service;

import com.ticketaca.auth.config.AuthProperties;
import com.ticketaca.auth.domain.AuthIdentity;
import com.ticketaca.auth.domain.AuthProvider;
import com.ticketaca.auth.domain.EmailVerification;
import com.ticketaca.auth.domain.EmailVerificationPurpose;
import com.ticketaca.auth.domain.Member;
import com.ticketaca.auth.domain.MemberRole;
import com.ticketaca.auth.domain.MemberStatus;
import com.ticketaca.auth.domain.RefreshToken;
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
import com.ticketaca.auth.repository.AuthIdentityRepository;
import com.ticketaca.auth.repository.EmailVerificationRepository;
import com.ticketaca.auth.repository.MemberRepository;
import com.ticketaca.auth.repository.RefreshTokenRepository;
import com.ticketaca.auth.security.JwtTokenProvider;
import com.ticketaca.global.exception.BusinessException;
import com.ticketaca.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private static final String EMAIL_PROVIDER_USER_ID_PREFIX = "email:";

    private final MemberRepository memberRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties authProperties;

    public AuthService(
            MemberRepository memberRepository,
            AuthIdentityRepository authIdentityRepository,
            EmailVerificationRepository emailVerificationRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthProperties authProperties
    ) {
        this.memberRepository = memberRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authProperties = authProperties;
    }

    @Transactional
    public EmailSignupResponse signup(EmailSignupRequest request, String requestedIp) {
        memberRepository.findByEmailAndDeletedAtIsNull(request.email())
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                });

        Member member = memberRepository.save(new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                MemberRole.MEMBER,
                MemberStatus.PENDING
        ));

        authIdentityRepository.save(new AuthIdentity(
                member.getId(),
                AuthProvider.EMAIL,
                EMAIL_PROVIDER_USER_ID_PREFIX + request.email(),
                request.email()
        ));

        emailVerificationRepository.save(new EmailVerification(
                member.getId(),
                request.email(),
                EmailVerificationPurpose.SIGNUP,
                generateVerificationCode(),
                LocalDateTime.now().plusMinutes(authProperties.emailVerification().expiresInMinutes()),
                requestedIp
        ));

        return new EmailSignupResponse(member.getId(), member.getEmail(), member.isEmailVerified());
    }

    @Transactional
    public EmailVerificationResponse verifyEmail(EmailVerificationRequest request) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeAndConsumedAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(
                        request.email(),
                        EmailVerificationPurpose.SIGNUP
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));

        LocalDateTime now = LocalDateTime.now();
        if (verification.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!verification.getVerificationCode().equals(request.verificationCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        Member member = memberRepository.findById(verification.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        verification.markVerified(now);
        member.verifyEmail();
        return new EmailVerificationResponse(member.getEmail(), true);
    }

    @Transactional
    public void resendVerificationCode(EmailVerificationResendRequest request, String requestedIp) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.isEmailVerified()) {
            return;
        }

        emailVerificationRepository.save(new EmailVerification(
                member.getId(),
                member.getEmail(),
                EmailVerificationPurpose.SIGNUP,
                generateVerificationCode(),
                LocalDateTime.now().plusMinutes(authProperties.emailVerification().expiresInMinutes()),
                requestedIp
        ));
    }

    @Transactional
    public TokenResponse login(EmailLoginRequest request) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!member.isEmailVerified() || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        member.updateLastLoginAt(LocalDateTime.now());
        return issueTokenPair(member);
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        Jws<Claims> claims = parseToken(request.refreshToken());
        ensureTokenType(claims, "refresh");
        Long memberId = Long.valueOf(claims.getPayload().getSubject());

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndDeletedAtIsNull(hashToken(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        LocalDateTime now = LocalDateTime.now();
        if (stored.isExpired(now) || stored.isRevoked()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        stored.revoke("ROTATED", now);
        return issueTokenPair(member);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNullAndDeletedAtIsNull(hashToken(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        stored.revoke("LOGOUT", LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public KakaoLoginEntrypointResponse kakaoEntrypoint(KakaoLoginRequest request) {
        if (request.authorizationCode().isBlank() || request.redirectUri().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return new KakaoLoginEntrypointResponse(
                "NOT_READY",
                ErrorCode.KAKAO_LOGIN_NOT_READY.getMessage()
        );
    }

    private TokenResponse issueTokenPair(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenRepository.save(new RefreshToken(
                member.getId(),
                hashToken(refreshToken),
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(authProperties.jwt().refreshTokenExpiresInSeconds()),
                null,
                null
        ));

        return new TokenResponse(
                accessToken,
                authProperties.jwt().accessTokenExpiresInSeconds(),
                refreshToken,
                authProperties.jwt().refreshTokenExpiresInSeconds()
        );
    }

    private Jws<Claims> parseToken(String token) {
        try {
            return jwtTokenProvider.parse(token);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void ensureTokenType(Jws<Claims> claims, String expectedType) {
        String tokenType = claims.getPayload().get("type", String.class);
        if (!expectedType.equals(tokenType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String generateVerificationCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is required", e);
        }
    }
}
