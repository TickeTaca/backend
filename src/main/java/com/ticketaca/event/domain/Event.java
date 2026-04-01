package com.ticketaca.event.domain;

import com.ticketaca.global.common.SoftDeleteBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventCategory category;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "booking_open_at", nullable = false)
    private LocalDateTime bookingOpenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Column(name = "cancel_deadline_hours", nullable = false)
    private int cancelDeadlineHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    public Event(
            Long venueId,
            String title,
            String description,
            EventCategory category,
            String thumbnailUrl,
            LocalDateTime bookingOpenAt,
            BookingStatus bookingStatus,
            int cancelDeadlineHours,
            EventStatus status
    ) {
        this.venueId = venueId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.bookingOpenAt = bookingOpenAt;
        this.bookingStatus = bookingStatus;
        this.cancelDeadlineHours = cancelDeadlineHours;
        this.status = status;
    }
}
