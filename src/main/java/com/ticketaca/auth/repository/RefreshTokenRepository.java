package com.ticketaca.auth.repository;

import com.ticketaca.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndDeletedAtIsNull(String tokenHash);

    long deleteByMemberIdAndExpiresAtBefore(Long memberId, LocalDateTime now);
}
