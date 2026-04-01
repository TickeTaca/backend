package com.ticketaca.auth.domain;

import com.ticketaca.global.common.SoftDeleteBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_identities_provider_user", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uk_auth_identities_member_provider", columnNames = {"member_id", "provider"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthIdentity extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 191)
    private String providerUserId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    public AuthIdentity(Long memberId, AuthProvider provider, String providerUserId, String providerEmail) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.linkedAt = LocalDateTime.now();
    }
}
