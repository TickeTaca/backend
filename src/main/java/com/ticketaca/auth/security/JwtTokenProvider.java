package com.ticketaca.auth.security;

import com.ticketaca.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String email, String role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.jwt().accessTokenExpiresInSeconds());
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(authProperties.jwt().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(Map.of(
                        "type", "access",
                        "email", email,
                        "role", role
                ))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.jwt().refreshTokenExpiresInSeconds());
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuer(authProperties.jwt().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
