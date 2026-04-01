package com.ticketaca.auth.repository;

import com.ticketaca.auth.domain.EmailVerification;
import com.ticketaca.auth.domain.EmailVerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeAndConsumedAtIsNullAndDeletedAtIsNullOrderByCreatedAtDesc(
            String email,
            EmailVerificationPurpose purpose
    );
}
