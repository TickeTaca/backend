# API 명세서

# 🎫 티키타카 (TickeTaca) — API 명세서

> 최종 수정일: 2026.02.24
> 
> 
> Base URL: `/api/v1`
> 
> 인증이 필요한 API는 🔒 표시, 관리자 전용은 🔑 표시
> 
> 실제 개발 시 SpringDoc(Swagger)으로 자동 생성되는 문서와 병행하여 사용합니다.
> 

---

## 목차

1. 공통 사항
2. 인증 API
3. 이벤트 API
4. 대기열 API
5. 좌석 API
6. 예매 API
7. 결제 API
8. 알림 API
9. 관리자 API
10. WebSocket 명세

---

## 1. 공통 사항

### 요청 헤더

| 헤더 | 값 | 필수 | 설명 |
| --- | --- | --- | --- |
| `Content-Type` | `application/json` | O |  |
| `Authorization` | `Bearer {accessToken}` | 🔒 API만 | JWT Access Token |
| `X-Entry-Token` | `{entryToken}` | 좌석/예매 API만 | 대기열 통과 후 발급받은 입장 토큰 |
| `X-Idempotency-Key` | `{uuid}` | 결제 API만 | 중복 요청 방지 |

### 공통 응답 형식

**성공 응답**

```json
{
  "status": "SUCCESS",
  "data": { ... }
}
```

**에러 응답**

```json
{
  "status": "ERROR",
  "error": {
    "code": "SEAT_ALREADY_HELD",
    "message": "이미 다른 사용자가 선택한 좌석입니다."
  },
  "timestamp": "2026-02-24T15:30:00"
}
```

### 페이지네이션 (커서 기반)

**요청 파라미터**

| 파라미터 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `cursor` | String | null | 이전 응답의 `nextCursor` 값 |
| `size` | Integer | 20 | 한 페이지당 항목 수 (최대 100) |

**응답 형식**

```json
{
  "data": {
    "content": [ ... ],
    "nextCursor": "eyJpZCI6MTAwfQ==",
    "hasNext": true
  }
}
```

---

## 2. 인증 API

### 2-1. 회원가입

`POST /api/v1/auth/signup`

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "티키타카유저"
}
```

**Response** `201 Created`

```json
{
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "티키타카유저"
  }
}
```

**에러 케이스**

| 상황 | 코드 | HTTP |
| --- | --- | --- |
| 이메일 중복 | `DUPLICATE_EMAIL` | 409 |
| 비밀번호 형식 불일치 | `INVALID_PASSWORD_FORMAT` | 400 |

---

### 2-2. 로그인

`POST /api/v1/auth/login`

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

**Response** `200 OK`

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 1800
  }
}
```

- Refresh Token은 `Set-Cookie` 헤더로 httpOnly 쿠키에 설정

---

### 2-3. 토큰 갱신

`POST /api/v1/auth/refresh`

- Refresh Token은 쿠키에서 자동 전송

**Response** `200 OK`

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 1800
  }
}
```

---

### 2-4. 로그아웃

🔒 `POST /api/v1/auth/logout`

**Response** `204 No Content`

- Refresh Token 쿠키 삭제

---

## 3. 이벤트 API

### 3-1. 이벤트 목록 조회

`GET /api/v1/events`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `category` | String | X | `CONCERT`, `SPORTS`, `MUSICAL`, `ETC` |
| `status` | String | X | `UPCOMING`, `BOOKING_OPEN`, `SOLD_OUT` |
| `dateFrom` | LocalDate | X | 공연 시작일 필터 |
| `dateTo` | LocalDate | X | 공연 종료일 필터 |
| `cursor` | String | X | 페이지네이션 커서 |
| `size` | Integer | X | 기본 20 |

**Response** `200 OK`

```json
{
  "data": {
    "content": [
      {
        "eventId": 1,
        "title": "2026 봄 콘서트",
        "category": "CONCERT",
        "venue": "올림픽공원 체조경기장",
        "eventDateTime": "2026-04-15T19:00:00",
        "bookingOpenAt": "2026-03-15T20:00:00",
        "thumbnailUrl": "https://cdn.example.com/images/event1.jpg",
        "status": "UPCOMING",
        "totalSeats": 15000,
        "availableSeats": 15000
      }
    ],
    "nextCursor": "eyJpZCI6MjB9",
    "hasNext": true
  }
}
```

---

### 3-2. 이벤트 상세 조회

`GET /api/v1/events/{eventId}`

**Response** `200 OK`

```json
{
  "data": {
    "eventId": 1,
    "title": "2026 봄 콘서트",
    "description": "봄을 맞이하여 진행하는 특별 콘서트입니다.",
    "category": "CONCERT",
    "venue": "올림픽공원 체조경기장",
    "eventDateTime": "2026-04-15T19:00:00",
    "bookingOpenAt": "2026-03-15T20:00:00",
    "thumbnailUrl": "https://cdn.example.com/images/event1.jpg",
    "status": "UPCOMING",
    "priceGrades": [
      { "gradeId": 1, "gradeName": "VIP", "price": 150000 },
      { "gradeId": 2, "gradeName": "R", "price": 120000 },
      { "gradeId": 3, "gradeName": "S", "price": 80000 }
    ],
    "sections": [
      {
        "sectionId": 1,
        "sectionName": "A구역",
        "gradeId": 1,
        "totalSeats": 200,
        "availableSeats": 185
      }
    ],
    "totalSeats": 15000,
    "availableSeats": 12350
  }
}
```

---

### 3-3. 이벤트 등록 (관리자)

🔑 `POST /api/v1/admin/events`

**Request Body**

```json
{
  "title": "2026 봄 콘서트",
  "description": "봄을 맞이하여 진행하는 특별 콘서트입니다.",
  "category": "CONCERT",
  "venue": "올림픽공원 체조경기장",
  "eventDateTime": "2026-04-15T19:00:00",
  "bookingOpenAt": "2026-03-15T20:00:00",
  "thumbnailUrl": "https://cdn.example.com/images/event1.jpg",
  "priceGrades": [
    { "gradeName": "VIP", "price": 150000 },
    { "gradeName": "R", "price": 120000 },
    { "gradeName": "S", "price": 80000 }
  ],
  "sections": [
    {
      "sectionName": "A구역",
      "gradeName": "VIP",
      "rows": 10,
      "columns": 20,
      "positionX": 0.3,
      "positionY": 0.2
    }
  ]
}
```

**Response** `201 Created`

```json
{
  "data": {
    "eventId": 1,
    "totalSeatsCreated": 15000
  }
}
```

---

### 3-4. 이벤트 수정 (관리자)

🔑 `PATCH /api/v1/admin/events/{eventId}`

**Request Body** (변경할 필드만 포함)

```json
{
  "title": "2026 봄 콘서트 (수정)",
  "bookingOpenAt": "2026-03-16T20:00:00"
}
```

**Response** `200 OK`

---

## 4. 대기열 API

### 4-1. 대기열 진입

🔒 `POST /api/v1/events/{eventId}/queue/enter`

**Request Body**: 없음

**Response** `200 OK`

```json
{
  "data": {
    "position": 1523,
    "estimatedWaitSeconds": 450,
    "totalWaiting": 8200
  }
}
```

**에러 케이스**

| 상황 | 코드 | HTTP |
| --- | --- | --- |
| 예매 오픈 전 | `BOOKING_NOT_OPEN` | 400 |
| 이미 대기열에 진입 | `ALREADY_IN_QUEUE` | 409 |
| 이미 예매 완료 | `DUPLICATE_BOOKING` | 409 |

---

### 4-2. 대기 순번 조회

🔒 `GET /api/v1/events/{eventId}/queue/status`

> JWT의 userId로 대기열 위치를 직접 조회합니다. 별도의 queueToken은 사용하지 않습니다.

**Response** `200 OK`

```json
{
  "data": {
    "status": "WAITING",
    "position": 823,
    "estimatedWaitSeconds": 240,
    "totalWaiting": 7500
  }
}
```

**status 값**:

| 상태 | 설명 | 프론트엔드 동작 |
| --- | --- | --- |
| `WAITING` | 대기 중 | 순번 표시, 폴링 계속 |
| `READY` | 입장 가능 | 입장 토큰 수신, 좌석 선택 페이지 이동 |
| `EXPIRED` | 대기 만료 | 만료 안내 + 재진입 유도 |

**READY 상태일 때 추가 응답 필드**:

```json
{
  "data": {
    "status": "READY",
    "entryToken": "eyJhbGciOiJIUzI1NiIs...",
    "entryTokenExpiresAt": "2026-03-15T20:15:00"
  }
}
```

---

### 4-3. 대기 순번 조회 (SSE) — 주 채널

🔒 `GET /api/v1/events/{eventId}/queue/stream`

> SSE가 대기열 알림의 주 채널입니다. SSE 연결이 불안정할 경우 4-2 폴링 API로 폴백합니다.
> JWT의 userId로 대기열 위치를 직접 조회합니다.

**SSE Event 형식**

```
event: queue-update
data: {"position":750,"estimatedWaitSeconds":210,"totalWaiting":7200}

event: entry-granted
data: {"entryToken":"eyJhbGciOiJIUzI1NiIs...","entryTokenExpiresAt":"2026-03-15T20:15:00"}
```

> **프론트엔드 구현 참고**: SSE를 주 채널로 사용합니다. SSE 연결이 불안정할 경우 4-2 폴링 API로 폴백합니다.
> 

---

### 4-4. 대기열 이탈

🔒 `DELETE /api/v1/events/{eventId}/queue`

> JWT의 userId로 대기열에서 제거합니다.

**Response** `204 No Content`

---

## 5. 좌석 API

### 5-1. 구역 목록 조회 (Overview)

🔒 `GET /api/v1/events/{eventId}/sections`

**필수 헤더**: `X-Entry-Token`

**Response** `200 OK`

```json
{
  "data": {
    "sections": [
      {
        "sectionId": 1,
        "sectionName": "A구역",
        "gradeName": "VIP",
        "price": 150000,
        "totalSeats": 200,
        "availableSeats": 185,
        "positionX": 0.3,
        "positionY": 0.2,
        "rows": 10,
        "columns": 20
      }
    ],
    "serverTime": "2026-03-15T20:05:30"
  }
}
```

---

### 5-2. 구역별 좌석 목록 조회

🔒 `GET /api/v1/events/{eventId}/sections/{sectionId}/seats`

**필수 헤더**: `X-Entry-Token`

**Response** `200 OK`

```json
{
  "data": {
    "sectionId": 1,
    "sectionName": "A구역",
    "seats": [
      {
        "seatId": 101,
        "row": 1,
        "col": 1,
        "status": "AVAILABLE"
      },
      {
        "seatId": 102,
        "row": 1,
        "col": 2,
        "status": "HELD"
      },
      {
        "seatId": 103,
        "row": 1,
        "col": 3,
        "status": "MY_HOLD",
        "holdExpireAt": "2026-03-15T20:10:30"
      },
      {
        "seatId": 104,
        "row": 1,
        "col": 4,
        "status": "RESERVED"
      }
    ],
    "serverTime": "2026-03-15T20:05:30"
  }
}
```

> 💡 **프론트엔드 참고**: `serverTime`을 활용하여 로컬 시간과의 offset을 계산하고, `holdExpireAt` 기반 카운트다운을 보정합니다.
> 

---

### 5-3. 좌석 선택 (임시 점유)

🔒 `POST /api/v1/events/{eventId}/seats/hold`

**필수 헤더**: `X-Entry-Token`

**Request Body**

```json
{
  "seatIds": [101, 102, 103]
}
```

**Response** `200 OK`

```json
{
  "data": {
    "holdId": "hold-550e8400-e29b-41d4",
    "seats": [
      { "seatId": 101, "row": 1, "col": 1, "status": "MY_HOLD", "price": 150000 },
      { "seatId": 102, "row": 1, "col": 2, "status": "MY_HOLD", "price": 150000 },
      { "seatId": 103, "row": 1, "col": 3, "status": "MY_HOLD", "price": 150000 }
    ],
    "holdExpireAt": "2026-03-15T20:10:30",
    "totalPrice": 450000,
    "priceSnapshotAt": "2026-03-15T20:05:30",
    "serverTime": "2026-03-15T20:05:30"
  }
}
```

**에러 케이스**

| 상황 | 코드 | HTTP |
| --- | --- | --- |
| 일부 좌석이 이미 점유/예매됨 | `SEAT_ALREADY_HELD` | 409 |
| 최대 4석 초과 | `MAX_SEAT_EXCEEDED` | 400 |
| 입장 토큰 만료 | `INVALID_ENTRY_TOKEN` | 403 |
| 이미 다른 좌석을 점유 중 | `ALREADY_HOLDING_SEATS` | 409 |

**409 에러 시 추가 응답** (충돌 좌석 정보):

```json
{
  "error": {
    "code": "SEAT_ALREADY_HELD",
    "message": "이미 다른 사용자가 선택한 좌석이 포함되어 있습니다.",
    "conflictSeatIds": [102]
  }
}
```

---

### 5-4. 좌석 선택 해제

🔒 `POST /api/v1/events/{eventId}/seats/release`

**필수 헤더**: `X-Entry-Token`

**Request Body**

```json
{
  "seatIds": [103]
}
```

- `seatIds`를 생략하면 본인이 점유 중인 전체 좌석 해제

**Response** `200 OK`

```json
{
  "data": {
    "releasedSeatIds": [103],
    "remainingHoldSeats": [
      { "seatId": 101, "row": 1, "col": 1 },
      { "seatId": 102, "row": 1, "col": 2 }
    ],
    "holdExpireAt": "2026-03-15T20:10:30"
  }
}
```

---

## 6. 예매 API

### 6-1. 예매 요청

🔒 `POST /api/v1/bookings`

**필수 헤더**: `X-Entry-Token`, `X-Idempotency-Key`

**Request Body**

```json
{
  "eventId": 1,
  "seatIds": [101, 102, 103]
}
```

**Response** `202 Accepted` (비동기 결제 처리 시작)

**Response Headers**:
- `Location: /api/v1/bookings/{bookingId}` — 예매 상태 조회 URL

> 결제 결과는 WebSocket `/user/queue/notifications` 채널로 `BOOKING_CONFIRMED` 또는 `PAYMENT_FAILED` 메시지로 알림됩니다.

```json
{
  "data": {
    "bookingId": "booking-550e8400-e29b-41d4",
    "status": "PAYMENT_PENDING",
    "totalPrice": 450000,
    "seats": [
      { "seatId": 101, "row": 1, "col": 1, "gradeName": "VIP", "price": 150000 },
      { "seatId": 102, "row": 1, "col": 2, "gradeName": "VIP", "price": 150000 },
      { "seatId": 103, "row": 1, "col": 3, "gradeName": "VIP", "price": 150000 }
    ]
  }
}
```

**에러 케이스**

| 상황 | 코드 | HTTP |
| --- | --- | --- |
| 점유 시간 만료 | `HOLD_EXPIRED` | 410 |
| 본인 점유 좌석이 아님 | `NOT_YOUR_HOLD` | 403 |
| 중복 요청 (동일 Idempotency Key) | `IDEMPOTENCY_CONFLICT` | 409 |

---

### 6-2. 예매 상태 조회

🔒 `GET /api/v1/bookings/{bookingId}`

**Response** `200 OK`

```json
{
  "data": {
    "bookingId": "booking-550e8400-e29b-41d4",
    "status": "CONFIRMED",
    "event": {
      "eventId": 1,
      "title": "2026 봄 콘서트",
      "venue": "올림픽공원 체조경기장",
      "eventDateTime": "2026-04-15T19:00:00"
    },
    "seats": [
      {
        "seatId": 101,
        "sectionName": "A구역",
        "row": 1,
        "col": 1,
        "gradeName": "VIP",
        "price": 150000
      }
    ],
    "totalPrice": 450000,
    "paymentId": "pay-123456",
    "bookedAt": "2026-03-15T20:07:15",
    "bookingNumber": "TCKT-20260315-00001"
  }
}
```

**예매 상태 (status)**:

| 상태 | 설명 |
| --- | --- |
| `PAYMENT_PENDING` | 결제 처리 중 |
| `CONFIRMED` | 예매 확정 (결제 완료) |
| `PAYMENT_FAILED` | 결제 실패 (좌석 자동 해제됨) |
| `CANCELLED` | 사용자가 취소 |

---

### 6-3. 내 예매 목록 조회

🔒 `GET /api/v1/bookings`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `status` | String | X | 상태 필터 |
| `cursor` | String | X | 페이지네이션 |
| `size` | Integer | X | 기본 20 |

**Response** `200 OK`

```json
{
  "data": {
    "content": [
      {
        "bookingId": "booking-550e8400-e29b-41d4",
        "bookingNumber": "TCKT-20260315-00001",
        "status": "CONFIRMED",
        "eventTitle": "2026 봄 콘서트",
        "eventDateTime": "2026-04-15T19:00:00",
        "venue": "올림픽공원 체조경기장",
        "seatCount": 3,
        "totalPrice": 450000,
        "bookedAt": "2026-03-15T20:07:15"
      }
    ],
    "nextCursor": "eyJpZCI6MjB9",
    "hasNext": false
  }
}
```

---

### 6-4. 예매 취소

🔒 `POST /api/v1/bookings/{bookingId}/cancel`

**Request Body**: 없음

**Response** `200 OK`

```json
{
  "data": {
    "bookingId": "booking-550e8400-e29b-41d4",
    "status": "CANCELLED",
    "refundAmount": 450000,
    "cancelledAt": "2026-03-20T10:30:00"
  }
}
```

**에러 케이스**

| 상황 | 코드 | HTTP |
| --- | --- | --- |
| 공연 24시간 이내 | `CANCEL_DEADLINE_PASSED` | 400 |
| 이미 취소된 예매 | `ALREADY_CANCELLED` | 409 |

---

## 7. 결제 API

> 💡 모의 결제 서비스입니다. 실제 PG사 연동 구조와 유사하게 설계하되, 내부적으로 랜덤 성공/실패를 반환합니다.
> 

### 7-1. 결제 상태 조회

🔒 `GET /api/v1/payments/{paymentId}`

**Response** `200 OK`

```json
{
  "data": {
    "paymentId": "pay-123456",
    "bookingId": "booking-550e8400-e29b-41d4",
    "status": "COMPLETED",
    "amount": 450000,
    "paidAt": "2026-03-15T20:07:12"
  }
}
```

**결제 상태**:

| 상태 | 설명 |
| --- | --- |
| `PENDING` | 결제 처리 중 |
| `COMPLETED` | 결제 완료 |
| `FAILED` | 결제 실패 |
| `REFUNDED` | 환불 완료 |

---

## 8. 알림 API

### 8-1. 알림 목록 조회

🔒 `GET /api/v1/notifications`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `cursor` | String | X | 페이지네이션 |
| `size` | Integer | X | 기본 20 |

**Response** `200 OK`

```json
{
  "data": {
    "content": [
      {
        "notificationId": 1,
        "type": "BOOKING_CONFIRMED",
        "title": "예매가 완료되었습니다",
        "message": "2026 봄 콘서트 A구역 1행 1~3번 좌석 예매가 확정되었습니다.",
        "isRead": false,
        "createdAt": "2026-03-15T20:07:15",
        "metadata": {
          "bookingId": "booking-550e8400-e29b-41d4",
          "eventId": 1
        }
      }
    ],
    "nextCursor": "eyJpZCI6MjB9",
    "hasNext": true
  }
}
```

**알림 타입 (type)**:

| 타입 | 설명 |
| --- | --- |
| `QUEUE_ENTRY_GRANTED` | 대기열 입장 허용 |
| `HOLD_EXPIRING_SOON` | 점유 만료 1분 전 |
| `HOLD_EXPIRED` | 점유 만료 |
| `BOOKING_CONFIRMED` | 예매 확정 |
| `PAYMENT_FAILED` | 결제 실패 |
| `BOOKING_CANCELLED` | 예매 취소 완료 |

---

### 8-2. 알림 읽음 처리

🔒 `PATCH /api/v1/notifications/{notificationId}/read`

**Response** `204 No Content`

---

### 8-3. 안읽은 알림 개수 조회

🔒 `GET /api/v1/notifications/unread-count`

**Response** `200 OK`

```json
{
  "data": {
    "count": 3
  }
}
```

---

## 9. 관리자 API

### 9-1. 대시보드 지표 조회

🔑 `GET /api/v1/admin/events/{eventId}/dashboard`

**Response** `200 OK`

```json
{
  "data": {
    "realTimeConnections": 3200,
    "queueSize": 5400,
    "seatStats": {
      "totalSeats": 15000,
      "availableSeats": 8200,
      "heldSeats": 1500,
      "reservedSeats": 5300,
      "salesRate": 35.3
    },
    "bookingsPerMinute": 85,
    "responseTime": {
      "p50": 120,
      "p95": 450,
      "p99": 980
    },
    "entrySpeed": {
      "batchSize": 50,
      "intervalSeconds": 3
    }
  }
}
```

---

### 9-2. 입장 속도 조절

🔑 `PATCH /api/v1/admin/events/{eventId}/queue/config`

**Request Body**

```json
{
  "batchSize": 100,
  "intervalSeconds": 2,
  "paused": false
}
```

**Response** `200 OK`

```json
{
  "data": {
    "batchSize": 100,
    "intervalSeconds": 2,
    "paused": false,
    "estimatedEntryRatePerMinute": 3000
  }
}
```

---

### 9-3. 이벤트 상태 변경

🔑 `PATCH /api/v1/admin/events/{eventId}/status`

**Request Body**

```json
{
  "action": "PAUSE_BOOKING"
}
```

**action 값**: `PAUSE_BOOKING` (예매 일시 중지), `RESUME_BOOKING` (예매 재개)

**Response** `200 OK`

---

## 10. WebSocket 명세

### 연결

**Endpoint**: `ws://{host}/ws`

**STOMP 프로토콜 사용**, SockJS fallback 지원

**연결 시 헤더**:

| 헤더 | 값 | 설명 |
| --- | --- | --- |
| `Authorization` | `Bearer {accessToken}` | JWT 인증 |
| `X-Entry-Token` | `{entryToken}` | 입장 토큰 |

---

### 구독 채널

### 좌석 상태 변경 구독

**채널**: `/topic/events/{eventId}/sections/{sectionId}`

**수신 메시지**:

```json
{
  "type": "SEAT_STATUS_CHANGED",
  "timestamp": "2026-03-15T20:05:35",
  "seats": [
    { "seatId": 101, "status": "HELD" },
    { "seatId": 102, "status": "HELD" }
  ]
}
```

**메시지 타입**:

| type | 설명 | 발생 시점 |
| --- | --- | --- |
| `SEAT_STATUS_CHANGED` | 좌석 상태 변경 | 점유, 해제, 예매 확정, 만료 시 |
| `SECTION_STATS_UPDATED` | 구역 통계 업데이트 | 잔여 좌석 수 변동 시 |

**`SECTION_STATS_UPDATED` 메시지**:

```json
{
  "type": "SECTION_STATS_UPDATED",
  "timestamp": "2026-03-15T20:05:35",
  "sectionId": 1,
  "availableSeats": 183,
  "totalSeats": 200
}
```

---

### 개인 알림 구독

**채널**: `/user/queue/notifications`

**수신 메시지**:

```json
{
  "type": "HOLD_EXPIRING_SOON",
  "timestamp": "2026-03-15T20:09:30",
  "message": "좌석 점유 시간이 1분 남았습니다.",
  "data": {
    "eventId": 1,
    "holdExpireAt": "2026-03-15T20:10:30"
  }
}
```

**개인 알림 타입**:

| type | 설명 |
| --- | --- |
| `HOLD_EXPIRING_SOON` | 점유 만료 1분 전 경고 |
| `HOLD_EXPIRED` | 점유 만료, 좌석 해제됨 |
| `BOOKING_CONFIRMED` | 예매 확정 완료 |
| `PAYMENT_FAILED` | 결제 실패, 좌석 해제됨 |

---

### 프론트엔드 WebSocket 연동 가이드

**연결 흐름**:

```
1. 좌석 선택 페이지 진입
2. SockJS + STOMP 연결 수립
3. 구역 뷰 진입 시: 구독 없음 (REST API로 데이터 조회)
4. 특정 구역 클릭 (좌석 뷰 진입):
   - REST API로 좌석 목록 조회
   - /topic/events/{eventId}/sections/{sectionId} 구독
5. 다른 구역으로 전환:
   - 이전 구역 구독 해제 (unsubscribe)
   - 새 구역 좌석 목록 REST 조회
   - 새 구역 구독 시작
6. 개인 알림 채널은 연결 시 즉시 구독 (페이지 이동과 무관)
7. 좌석 선택 페이지 이탈: 모든 구독 해제 + 연결 종료
```

**재연결 전략**:

- 연결 끊김 감지 시 자동 재연결 시도 (최대 5회, 3초 간격)
- 재연결 성공 시 이전 구독 채널 자동 복구
- 재연결 후 REST API로 좌석 상태 전체 동기화 (WebSocket 끊긴 동안의 변경분 보정)

---

## 부록: API 엔드포인트 요약표

| Method | Endpoint | 인증 | 설명 |
| --- | --- | --- | --- |
| **인증** |  |  |  |
| POST | `/api/v1/auth/signup` | - | 회원가입 |
| POST | `/api/v1/auth/login` | - | 로그인 |
| POST | `/api/v1/auth/refresh` | Cookie | 토큰 갱신 |
| POST | `/api/v1/auth/logout` | 🔒 | 로그아웃 |
| **이벤트** |  |  |  |
| GET | `/api/v1/events` | - | 이벤트 목록 조회 |
| GET | `/api/v1/events/{eventId}` | - | 이벤트 상세 조회 |
| **대기열** |  |  |  |
| POST | `/api/v1/events/{eventId}/queue/enter` | 🔒 | 대기열 진입 |
| GET | `/api/v1/events/{eventId}/queue/status` | 🔒 | 순번 조회 (폴링) |
| GET | `/api/v1/events/{eventId}/queue/stream` | 🔒 | 순번 조회 (SSE) |
| DELETE | `/api/v1/events/{eventId}/queue` | 🔒 | 대기열 이탈 |
| **좌석** |  |  |  |
| GET | `/api/v1/events/{eventId}/sections` | 🔒+🎟 | 구역 목록 조회 |
| GET | `/api/v1/events/{eventId}/sections/{sectionId}/seats` | 🔒+🎟 | 좌석 목록 조회 |
| POST | `/api/v1/events/{eventId}/seats/hold` | 🔒+🎟 | 좌석 점유 |
| POST | `/api/v1/events/{eventId}/seats/release` | 🔒+🎟 | 좌석 해제 |
| **예매** |  |  |  |
| POST | `/api/v1/bookings` | 🔒+🎟 | 예매 요청 |
| GET | `/api/v1/bookings/{bookingId}` | 🔒 | 예매 상세 조회 |
| GET | `/api/v1/bookings` | 🔒 | 내 예매 목록 |
| POST | `/api/v1/bookings/{bookingId}/cancel` | 🔒 | 예매 취소 |
| **결제** |  |  |  |
| GET | `/api/v1/payments/{paymentId}` | 🔒 | 결제 상태 조회 |
| **알림** |  |  |  |
| GET | `/api/v1/notifications` | 🔒 | 알림 목록 |
| PATCH | `/api/v1/notifications/{id}/read` | 🔒 | 읽음 처리 |
| GET | `/api/v1/notifications/unread-count` | 🔒 | 안읽은 수 |
| **관리자** |  |  |  |
| POST | `/api/v1/admin/events` | 🔑 | 이벤트 등록 |
| PATCH | `/api/v1/admin/events/{eventId}` | 🔑 | 이벤트 수정 |
| PATCH | `/api/v1/admin/events/{eventId}/status` | 🔑 | 이벤트 상태 변경 |
| GET | `/api/v1/admin/events/{eventId}/dashboard` | 🔑 | 대시보드 |
| PATCH | `/api/v1/admin/events/{eventId}/queue/config` | 🔑 | 입장 속도 조절 |

> 🔒 = JWT 인증 필요 | 🎟 = 입장 토큰(X-Entry-Token) 필요 | 🔑 = ADMIN 권한 필요
>