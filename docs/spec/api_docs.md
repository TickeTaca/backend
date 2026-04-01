# API Docs

# TickeTaca API 명세

> 최종 수정일: 2026-03-25
>
> Base URL: `/api/v1`
>
> 이 문서는 현재 합의된 백엔드 구현 기준을 설명합니다.
> 실제 프론트엔드 협업의 기준 문서는 SpringDoc OpenAPI와 Swagger UI입니다.

---

## 1. 문서 목적

- 프론트엔드와 협업할 때 사용할 1차 API 계약 문서입니다.
- 현재 범위에 포함된 기능만 다룹니다.
- 상세 필드 정의는 Swagger를 우선 기준으로 하고, 이 문서는 흐름과 정책을 설명합니다.

## 2. 공통 규칙

### 2-1. 인증 헤더

| 헤더 | 값 예시 | 설명 |
| --- | --- | --- |
| `Authorization` | `Bearer {accessToken}` | 회원 전용 API 호출 시 사용 |
| `X-Admission-Token` | `{admissionToken}` | 대기열 통과 후 좌석 점유/주문 API 호출 시 사용 |
| `X-Idempotency-Key` | `{uuid}` | 결제 확인, 주문 생성, 웹훅 처리 시 중복 방지 |

### 2-2. 공통 응답 형식

성공:

```json
{
  "status": "SUCCESS",
  "data": {}
}
```

실패:

```json
{
  "status": "ERROR",
  "error": {
    "code": "SEAT_ALREADY_HELD",
    "message": "이미 다른 사용자가 점유 중인 좌석입니다."
  },
  "timestamp": "2026-03-25T20:00:00+09:00"
}
```

### 2-3. 인증 정책

- Access Token 만료 시간: 15분
- Refresh Token 만료 시간: 14일
- Refresh Token은 서버 저장소에 해시 형태로 저장합니다.
- 이메일 회원가입은 이메일 인증 완료 후 활성화됩니다.
- 소셜 로그인은 카카오 OAuth2 Authorization Code 흐름을 사용합니다.

### 2-4. 예매 관련 공통 정책

- 지정 좌석 hold TTL: 5분
- Admission Token TTL: 10분
- 인기 공연에만 대기열을 활성화합니다.
- 기본 취소 마감: 공연 시작 24시간 전
- 부분 취소와 부분 환불은 현재 범위에서 제외합니다.

---

## 3. 인증 API

### 3-1. 이메일 회원가입

`POST /auth/email/signup`

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "홍길동"
}
```

Response `201 Created`:

```json
{
  "status": "SUCCESS",
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "emailVerified": false
  }
}
```

### 3-2. 이메일 인증

`POST /auth/email/verify`

Request:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

### 3-2-1. 이메일 인증 코드 재전송

`POST /auth/email/verification/resend`

Request:

```json
{
  "email": "user@example.com"
}
```

### 3-3. 이메일 로그인

`POST /auth/email/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "accessToken": "jwt-access-token",
    "accessTokenExpiresInSeconds": 900,
    "refreshToken": "jwt-refresh-token",
    "refreshTokenExpiresInSeconds": 1209600
  }
}
```

### 3-4. 카카오 로그인

`POST /auth/social/kakao`

Request:

```json
{
  "authorizationCode": "kakao-auth-code",
  "redirectUri": "https://frontend.example.com/login/kakao/callback"
}
```

### 3-5. 토큰 재발급

`POST /auth/token/refresh`

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### 3-6. 로그아웃

`POST /auth/logout`

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

---

## 4. 공연 카탈로그 API

### 4-1. 공연 목록 조회

`GET /events`

Query Parameters:

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `category` | String | 공연 카테고리 필터 |
| `venue` | String | 공연장 필터 |
| `dateFrom` | LocalDate | 회차 시작일 범위 |
| `dateTo` | LocalDate | 회차 종료일 범위 |
| `status` | String | `UPCOMING`, `BOOKING_OPEN`, `SOLD_OUT` |
| `page` | Integer | 기본 0 |
| `size` | Integer | 기본 20, 최대 100 |
| `sort` | String | `bookingOpenAt,asc` 등 |

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "eventId": 1,
        "title": "2026 봄 콘서트",
        "category": "CONCERT",
        "venueName": "올림픽체조경기장",
        "bookingOpenAt": "2026-04-01T20:00:00+09:00",
        "thumbnailUrl": "https://cdn.example.com/events/1.png",
        "status": "BOOKING_OPEN"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

### 4-2. 공연 상세 조회

`GET /events/{eventId}`

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "eventId": 1,
    "title": "2026 봄 콘서트",
    "description": "공연 소개",
    "category": "CONCERT",
    "venueName": "올림픽체조경기장",
    "thumbnailUrl": "https://cdn.example.com/events/1.png",
    "bookingOpenAt": "2026-04-01T20:00:00+09:00",
    "cancelDeadlinePolicy": {
      "type": "HOURS_BEFORE_SESSION",
      "hours": 24
    },
    "sessions": [
      {
        "sessionId": 1001,
        "startsAt": "2026-04-15T19:00:00+09:00",
        "endsAt": "2026-04-15T21:30:00+09:00",
        "bookingStatus": "BOOKING_OPEN"
      }
    ]
  }
}
```

### 4-3. 회차 좌석/재고 현황 조회

`GET /sessions/{sessionId}/availability`

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "sessionId": 1001,
    "seatMode": "ASSIGNED",
    "priceGrades": [
      {
        "priceGradeId": 1,
        "name": "VIP",
        "price": 150000
      }
    ],
    "sections": [
      {
        "sectionId": 10,
        "name": "A구역",
        "availableCount": 180,
        "totalCount": 200
      }
    ]
  }
}
```

---

## 5. 대기열 API

### 5-1. 대기열 진입

`POST /queue/enter`

Request:

```json
{
  "sessionId": 1001
}
```

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "queueId": "queue-01",
    "position": 1523,
    "estimatedWaitSeconds": 420
  }
}
```

### 5-2. 대기열 스트림 구독

`GET /queue/stream?sessionId=1001`

응답은 `text/event-stream`입니다.

예시:

```text
event: queue-update
data: {"position":1200,"estimatedWaitSeconds":320}

event: admission-granted
data: {"admissionToken":"token-value","expiresAt":"2026-04-01T20:10:00+09:00"}
```

설명:

- 대기열은 SSE를 기본 방식으로 사용합니다.
- 프론트엔드는 이 스트림을 기준으로 대기 상태와 입장 허가를 처리합니다.
- 인기 공연이 아닌 경우 대기열 없이 바로 예매 흐름으로 진입할 수 있습니다.

---

## 6. 예매 API

### 6-1. 좌석 hold 생성

`POST /booking/holds`

필수 헤더:

- `Authorization`
- `X-Admission-Token`

Request:

```json
{
  "sessionId": 1001,
  "type": "ASSIGNED_SEAT",
  "seatIds": [101, 102]
}
```

비지정석 예시:

```json
{
  "sessionId": 1001,
  "type": "GENERAL_ADMISSION",
  "sectionId": 201,
  "quantity": 2
}
```

Response `201 Created`:

```json
{
  "status": "SUCCESS",
  "data": {
    "holdId": "hold-123",
    "sessionId": 1001,
    "expiresAt": "2026-04-01T20:05:00+09:00",
    "items": [
      {
        "seatId": 101,
        "label": "A구역 1열 1번",
        "price": 150000
      }
    ],
    "totalAmount": 300000
  }
}
```

### 6-2. 좌석 hold 취소

`DELETE /booking/holds/{holdId}`

필수 헤더:

- `Authorization`
- `X-Admission-Token`

Response `204 No Content`

### 6-3. 주문 생성

`POST /orders`

필수 헤더:

- `Authorization`
- `X-Admission-Token`
- `X-Idempotency-Key`

Request:

```json
{
  "holdId": "hold-123",
  "buyerName": "홍길동",
  "buyerEmail": "user@example.com",
  "buyerPhone": "01012345678"
}
```

Response `201 Created`:

```json
{
  "status": "SUCCESS",
  "data": {
    "orderId": "order-123",
    "status": "HELD",
    "amount": 300000,
    "paymentMethod": "TOSS_PAYMENTS"
  }
}
```

### 6-4. 주문 취소

`POST /orders/{orderId}/cancel`

필수 헤더:

- `Authorization`

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "orderId": "order-123",
    "status": "CANCELLED",
    "refundAmount": 300000
  }
}
```

---

## 7. 결제 API

### 7-1. 토스 결제 승인

`POST /payments/toss/confirm`

필수 헤더:

- `Authorization`
- `X-Idempotency-Key`

Request:

```json
{
  "orderId": "order-123",
  "paymentKey": "toss-payment-key",
  "amount": 300000
}
```

Response `200 OK`:

```json
{
  "status": "SUCCESS",
  "data": {
    "paymentId": "payment-123",
    "orderId": "order-123",
    "status": "PAID",
    "approvedAt": "2026-04-01T20:03:00+09:00"
  }
}
```

### 7-2. 토스 웹훅 수신

`POST /payments/toss/webhook`

설명:

- 결제 승인/취소 이벤트를 수신합니다.
- 웹훅은 반드시 멱등하게 처리합니다.
- 클라이언트 confirm API와 웹훅은 순서가 바뀌어 도착할 수 있습니다.

---

## 8. 관리자 API

설명:

- 관리자 API는 내부 운영용입니다.
- 공연, 회차, 좌석맵, 가격 등급, 구역 재고, 대기열 정책, 취소 정책을 관리합니다.

대표 예시:

- `POST /admin/events`
- `POST /admin/events/{eventId}/sessions`
- `POST /admin/sessions/{sessionId}/price-grades`
- `POST /admin/sessions/{sessionId}/seat-map`
- `POST /admin/sessions/{sessionId}/queue-policy`
- `PATCH /admin/events/{eventId}/cancel-policy`

---

## 9. 상태 머신

주문 상태:

- `CREATED`
- `HELD`
- `PAYMENT_PENDING`
- `PAID`
- `CANCELLED`
- `EXPIRED`
- `REFUNDED`

결제 상태:

- `READY`
- `PENDING`
- `PAID`
- `FAILED`
- `CANCELLED`
- `REFUNDED`

---

## 10. 제외 범위

현재 문서 범위에 포함하지 않는 기능:

- 쿠폰
- 포인트
- 부분 취소
- 부분 환불
- 정산
- 추천
- 2차 거래
- 다국어
- 전용 검색 엔진

---

## 11. 프론트엔드 협업 메모

- OpenAPI를 프론트엔드의 타입 생성 기준으로 사용합니다.
- 기능 구현이 끝나는 단위마다 Swagger가 깨지지 않는지 함께 검증합니다.
- 우선 배포가 가능한 기능은 `auth`, `event catalog`, `admin event/session setup` 순서로 나눠서 공개합니다.
