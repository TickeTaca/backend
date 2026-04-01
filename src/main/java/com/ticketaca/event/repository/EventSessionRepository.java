package com.ticketaca.event.repository;

import com.ticketaca.event.domain.EventSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventSessionRepository extends JpaRepository<EventSession, Long> {

    List<EventSession> findByEventIdAndStartsAtBetweenAndDeletedAtIsNull(
            Long eventId,
            LocalDateTime from,
            LocalDateTime to
    );
}
