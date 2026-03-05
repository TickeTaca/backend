package com.ticketaca.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Redis 키 패턴 상수 정의.
 * 모든 Redis 키는 이 클래스를 통해 생성하며, 하드코딩하지 않는다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeyConstants {

    /** 좌석 점유 키: hold:{eventId}:{seatId} → value: userId, TTL: 300초 */
    private static final String HOLD_KEY_PREFIX = "hold:%d:%d";

    /** 대기열 Sorted Set: queue:{eventId} → score: timestamp */
    private static final String QUEUE_KEY_PREFIX = "queue:%d";

    /** 대기열 위치 캐시: queue:position:{userId}:{eventId} */
    private static final String QUEUE_POSITION_PREFIX = "queue:position:%d:%d";

    /** 입장 토큰 발급 여부: entry:token:{userId}:{eventId} */
    private static final String ENTRY_TOKEN_PREFIX = "entry:token:%d:%d";

    /** 멱등성 키: idempotency:{key} → TTL: 86400초 */
    private static final String IDEMPOTENCY_PREFIX = "idempotency:%s";

    /** 이벤트 상세 캐시: event:cache:{eventId} */
    private static final String EVENT_CACHE_PREFIX = "event:cache:%d";

    public static final int HOLD_TTL_SECONDS = 300;
    public static final int IDEMPOTENCY_TTL_SECONDS = 86_400;
    public static final int MAX_HOLD_SEATS = 4;

    public static String holdKey(Long eventId, Long seatId) {
        return String.format(HOLD_KEY_PREFIX, eventId, seatId);
    }

    public static String queueKey(Long eventId) {
        return String.format(QUEUE_KEY_PREFIX, eventId);
    }

    public static String queuePositionKey(Long userId, Long eventId) {
        return String.format(QUEUE_POSITION_PREFIX, userId, eventId);
    }

    public static String entryTokenKey(Long userId, Long eventId) {
        return String.format(ENTRY_TOKEN_PREFIX, userId, eventId);
    }

    public static String idempotencyKey(String key) {
        return String.format(IDEMPOTENCY_PREFIX, key);
    }

    public static String eventCacheKey(Long eventId) {
        return String.format(EVENT_CACHE_PREFIX, eventId);
    }
}
