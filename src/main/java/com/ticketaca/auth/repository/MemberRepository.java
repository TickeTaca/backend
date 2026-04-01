package com.ticketaca.auth.repository;

import com.ticketaca.auth.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);
}
