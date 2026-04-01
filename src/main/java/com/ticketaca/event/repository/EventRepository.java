package com.ticketaca.event.repository;

import com.ticketaca.event.domain.Event;
import com.ticketaca.event.domain.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategoryAndDeletedAtIsNull(EventCategory category);
}
