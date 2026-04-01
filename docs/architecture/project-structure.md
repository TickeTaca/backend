# Project Structure

> 최종 수정일: 2026-03-25

---

## 1. 문서 목적

이 문서는 현재 저장소 구조와 목표 아키텍처를 함께 설명합니다.
학습 목적 프로젝트이므로, 현재 코드 구조와 장기 설계를 분리해서 이해하는 것이 중요합니다.

---

## 2. 현재 전략

현재 저장소는 하나의 Spring Boot 애플리케이션으로 유지합니다.
하지만 패키지 경계는 Spring Modulith 스타일로 나누고, 나중에 대기열을 별도 서비스로 분리할 수 있게 설계합니다.

이 선택의 trade-off는 다음과 같습니다.

- 장점:
  - 초기 학습 비용이 낮습니다.
  - 기능을 슬라이스 단위로 구현하기 쉽습니다.
  - 도메인 모델과 API 계약을 먼저 안정화할 수 있습니다.
- 단점:
  - 초고트래픽 대기열을 독립적으로 확장하기는 어렵습니다.
  - 서비스별 배포 주기를 분리하기 어렵습니다.

결론:

- 지금은 단일 애플리케이션 구조가 적절합니다.
- 대기열은 가장 먼저 분리 가능한 후보로 유지합니다.

---

## 3. 목표 아키텍처

### 3-1. Target

- Core API
  - 회원
  - 공연 카탈로그
  - 관리자
  - 예매
  - 주문
  - 결제
- Queue Service
  - 대기열 진입
  - 순번 계산
  - SSE 스트림
  - Admission Token 발급

### 3-2. Infra

- AWS Seoul Region
- Aurora MySQL
- Redis Cluster
- MSK Serverless
- ECS Fargate
- ALB

---

## 4. 현재 패키지 구조

```text
com.ticketaca
├─ global
├─ auth
├─ event
├─ queue
├─ booking
├─ payment
├─ admin
└─ notification
```

### 4-1. `global`

- 공통 설정
- 예외 처리
- 공통 응답
- 공통 상수
- 보안/문서화/관측성 설정

### 4-2. `auth`

- 이메일 회원가입
- 이메일 인증
- 카카오 로그인
- JWT / Refresh Token
- 회원 식별 정보

### 4-3. `event`

- Event
- Session
- Seat
- SectionInventory
- PriceGrade
- 공개 카탈로그 조회

### 4-4. `queue`

- QueuePolicy
- QueueEntry
- AdmissionToken
- 대기열 순번 계산
- SSE 발행

### 4-5. `booking`

- SeatHold
- GeneralAdmissionHold
- Order 생성 진입점
- 좌석/재고 점유 규칙

### 4-6. `payment`

- Payment
- Refund
- Toss confirm
- Toss webhook
- 멱등 처리

### 4-7. `admin`

- 공연 관리
- 회차 관리
- 좌석맵 관리
- 가격 등급 관리
- 대기열 정책 관리
- 취소 정책 관리

### 4-8. `notification`

- Outbox 소비
- 알림 발행
- 감사 로그
- 캐시 무효화 이벤트 처리

---

## 5. 패키지 의존성 원칙

- 모든 패키지는 `global`을 공통 기반으로 사용할 수 있습니다.
- 다른 도메인의 controller를 직접 호출하지 않습니다.
- 도메인 간 연동은 service interface, domain event, explicit port로 제한합니다.
- `queue`는 `booking`과 강하게 결합하지 않고 Admission Token 기반으로만 연결합니다.
- `payment`는 `booking/order` 상태 변경을 호출하더라도 멱등성과 재처리를 우선 고려합니다.

---

## 6. 디렉터리 운영 원칙

### 6-1. 코드

- `src/main/java/com/ticketaca/{module}` 아래에 모듈별 코드를 둡니다.
- 모듈 내부는 `controller`, `service`, `domain`, `repository`, `dto` 중심으로 정리합니다.

### 6-2. 문서

- `docs/spec`
  - proposal
  - functional spec
  - api docs
- `docs/architecture`
  - 구조와 아키텍처 의사결정
  - database schema(Soft FK 규약)
- 이후 필요 시 `docs/adr`, `docs/troubleshooting`, `docs/load-test`를 추가합니다.

---

## 7. 구현 순서와 구조 반영

현재 구조에서 다음 순서로 구현하는 것을 권장합니다.

1. `event` + `admin`
2. `auth`
3. `queue`
4. `booking`
5. `payment`
6. `notification`

이 순서의 이유:

- 프론트엔드가 먼저 붙을 수 있는 조회/관리 API를 빠르게 확보할 수 있습니다.
- 좌석/회차 모델이 안정된 뒤 hold와 주문 설계가 쉬워집니다.
- 결제와 비동기 이벤트는 앞선 도메인 모델이 정리된 뒤 붙이는 편이 안전합니다.

---

## 8. 프론트엔드 협업 포인트

- Swagger/OpenAPI를 항상 최신 상태로 유지합니다.
- 슬라이스 단위로 배포 가능한 API부터 우선 공개합니다.
- 좌석 관련 API는 응답 필드 변경이 프론트 영향이 크므로 변경 시 문서와 Swagger를 반드시 함께 갱신합니다.
