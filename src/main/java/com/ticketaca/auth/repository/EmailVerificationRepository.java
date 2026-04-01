package com.ticketaca.auth.repository;

import com.ticketaca.auth.domain.EmailVerification;
import com.ticketaca.auth.domain.EmailVerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String email,
            EmailVerificationPurpose purpose,
            LocalDateTime now
    );
}
