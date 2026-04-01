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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "event_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_sessions_event_starts_at", columnNames = {"event_id", "starts_at"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventSession extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_mode", nullable = false, length = 20)
    private SeatMode seatMode;

    public EventSession(
            Long eventId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            BookingStatus bookingStatus,
            SeatMode seatMode
    ) {
        this.eventId = eventId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.bookingStatus = bookingStatus;
        this.seatMode = seatMode;
    }
}
