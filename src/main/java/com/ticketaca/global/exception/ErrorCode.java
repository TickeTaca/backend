package com.ticketaca.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_ENTRY_TOKEN(HttpStatus.FORBIDDEN, "INVALID_ENTRY_TOKEN", "유효하지 않은 입장 토큰입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_FORMAT", "비밀번호 형식이 올바르지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "존재하지 않는 회원입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", "인증 코드가 올바르지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "VERIFICATION_CODE_EXPIRED", "인증 코드가 만료되었습니다."),
    KAKAO_LOGIN_NOT_READY(HttpStatus.NOT_IMPLEMENTED, "KAKAO_LOGIN_NOT_READY", "카카오 로그인 연동 준비 중입니다."),

    // Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "존재하지 않는 이벤트입니다."),

    // Seat
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "이미 다른 사용자가 선택한 좌석입니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "SEAT_ALREADY_RESERVED", "이미 예매 완료된 좌석입니다."),
    MAX_SEAT_EXCEEDED(HttpStatus.BAD_REQUEST, "MAX_SEAT_EXCEEDED", "최대 좌석 선택 수를 초과했습니다."),
    ALREADY_HOLDING_SEATS(HttpStatus.CONFLICT, "ALREADY_HOLDING_SEATS", "이미 다른 좌석을 점유 중입니다."),
    HOLD_EXPIRED(HttpStatus.GONE, "HOLD_EXPIRED", "좌석 점유 시간이 만료되었습니다."),

    // Queue
    BOOKING_NOT_OPEN(HttpStatus.BAD_REQUEST, "BOOKING_NOT_OPEN", "예매 오픈 전입니다."),
    QUEUE_ENTRY_REQUIRED(HttpStatus.FORBIDDEN, "QUEUE_ENTRY_REQUIRED", "대기열에 진입하지 않았습니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "ALREADY_IN_QUEUE", "이미 대기열에 진입했습니다."),

    // Booking
    DUPLICATE_BOOKING(HttpStatus.CONFLICT, "DUPLICATE_BOOKING", "이미 예매가 완료된 이벤트입니다."),
    NOT_YOUR_HOLD(HttpStatus.FORBIDDEN, "NOT_YOUR_HOLD", "본인이 점유 중인 좌석이 아닙니다."),
    ALREADY_CANCELLED(HttpStatus.CONFLICT, "ALREADY_CANCELLED", "이미 취소된 예매입니다."),
    CANCEL_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "CANCEL_DEADLINE_PASSED", "취소 가능 기한이 지났습니다."),

    // Payment
    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "PAYMENT_FAILED", "결제에 실패했습니다."),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "중복 결제 요청입니다."),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "요청 횟수를 초과했습니다."),

    // Internal
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
