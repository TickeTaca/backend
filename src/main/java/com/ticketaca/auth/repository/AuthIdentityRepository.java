package com.ticketaca.auth.repository;

import com.ticketaca.auth.domain.AuthIdentity;
import com.ticketaca.auth.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    Optional<AuthIdentity> findByProviderAndProviderUserIdAndDeletedAtIsNull(AuthProvider provider, String providerUserId);

    boolean existsByMemberIdAndProviderAndDeletedAtIsNull(Long memberId, AuthProvider provider);
}
