package com.ticketaca.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Kafka Topic 이름 상수 정의.
 * Topic 네이밍: ticketaca.{domain}.{event} (dot-notation)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopicConstants {

    public static final String BOOKING_REQUESTED = "ticketaca.booking.requested";
    public static final String BOOKING_CONFIRMED = "ticketaca.booking.confirmed";
    public static final String BOOKING_CANCELLED = "ticketaca.booking.cancelled";
    public static final String PAYMENT_COMPLETED = "ticketaca.payment.completed";
    public static final String PAYMENT_FAILED = "ticketaca.payment.failed";
    public static final String SEAT_HELD = "ticketaca.seat.held";
    public static final String SEAT_RELEASED = "ticketaca.seat.released";
}
