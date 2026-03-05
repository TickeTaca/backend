# REQUIREMENTS.md

> 본 문서는 티키타카(TickeTaca) 프로젝트 전반에 걸쳐 **모든 코드, 설계, 리뷰에서 보장되어야 하는 비기능적 요구사항과 품질 기준**을 정의합니다.
>
> 새로운 기능을 구현하거나, 기존 코드를 수정할 때 반드시 이 문서의 기준을 충족하는지 확인합니다.

---
## Core System Requirements - 공통 요구사항
> 프로젝트의 모든 코드 작성 및 리뷰 시 기본적으로 아래 공통 요구사항을 따릅니다.
### TickeTaca (티키타카) - Core System Requirements

### 1. Project Overview
'티키타카(TickeTaca)'는 대규모 트래픽이 발생하는 티켓 예매 상황(Ticketing Open)을 가정하고, 이를 안정적으로 처리할 수 있는 고가용성/고동시성 백엔드 시스템을 구축하는 프로젝트입니다. AI를 활용하여 구현 속도를 극대화하고, 남는 리소스를 아키텍처 설계와 깊이 있는 CS/시스템 최적화 학습에 집중합니다.

### 2. Core Design Principles (핵심 설계 원칙)
* **Design for Failure:** 모든 컴포넌트는 언제든 실패할 수 있음을 가정하고 설계합니다. (재시도 로직, Circuit Breaker, Fallback)
* **Trade-off Documentation:** 기술 도입 시 반드시 대안을 검토하고, 왜 이 기술을 선택했는지(Trade-off)를 문서화합니다. (면접 대비용)
  * 문서는 `/docs/adr/` 하위에 `0000-adr-template.md`의 형식을 지켜 마크다운 문서로 작성합니다.
  * '어떤 기술을 썼는가'보다 '왜 그 기술을 선택했고, 어떤 대안들을 검토했는가'를 작성합니다.
  * 이 문서들은 Git에 트래킹되지 않도록 합니다.
* **Stateless Architecture:** Scale-out이 용이하도록 애플리케이션 서버는 무상태(Stateless)를 유지합니다.

### 3. Non-Functional Requirements (비기능적 요구사항)

#### 3.1. 고동시성 처리 및 데이터 정합성 (Concurrency & Consistency)
* **No Double Booking:** 초당 만 단위의 요청이 몰리더라도 동일한 좌석이 중복 예매되는 일은 절대 없어야 합니다.
* **Locking Strategy:** 상황에 맞는 적절한 락(Optimistic/Pessimistic Lock, Redis Distributed Lock 등)을 평가하고 적용합니다.
* **Idempotency (멱등성):** 결제 및 예매 요청 시 네트워크 지연이나 재시도로 인한 중복 결제를 방지하기 위해 멱등성을 보장해야 합니다.

#### 3.2. 성능 및 가용성 (Performance & Availability)
* **Traffic Spikes Handling:** 티켓 오픈 시점의 대규모 트래픽(Spike)을 데이터베이스로 직접 흘려보내지 않도록 대기열(Queue) 시스템이나 캐싱 전략을 필수적으로 적용합니다.
* **Response Time:** 사용자의 주요 API(좌석 조회 등) 응답 시간은 P95 기준 200ms 이내를 목표로 합니다.
* **SPOF (Single Point of Failure) 제거:** DB, Cache, Message Broker 등 주요 인프라의 단일 장애점을 식별하고 이중화/클러스터링 구조를 고려합니다.

#### 3.3. 관측 가능성 (Observability)
* **Logging:** 에러 발생 시 추적이 가능하도록 Correlation ID를 포함한 중앙 집중식 로깅을 구축합니다.
* **Monitoring:** 시스템 지표(CPU, Memory, Connection Pool 등)와 비즈니스 지표(초당 예매 성공률 등)를 모니터링할 수 있어야 합니다.

### 4. Domain Boundaries (핵심 도메인)
1.  **User (사용자):** 인증/인가, 마이페이지
2.  **Performance (공연):** 공연 정보, 회차, 좌석 메타데이터 관리 (Read Heavy)
3.  **Ticketing (예매):** 좌석 선점, 대기열, 예매 상태 전환 (Write Heavy, High Concurrency)
4.  **Payment (결제):** 결제 승인/취소, 외부 PG사 연동 모의

> 성능 요구사항 및 비기능적 요구사항은 아래 구체적으로 서술되는 10개 분류의 요구사항을 모두 필수로 충족해야 합니다.

## 1. 성능 요구사항

### 1-1. 응답 시간

| 구분 | p50 | p95 | p99 | 비고 |
| --- | --- | --- | --- | --- |
| 일반 조회 API (이벤트 목록/상세) | 50ms | 150ms | 300ms | Redis 캐시 적용 대상 |
| 좌석맵 조회 | 100ms | 300ms | 500ms | 구역별 Lazy Load |
| 좌석 점유 (Hold) | 50ms | 100ms | 200ms | Redis Lua Script 기준 |
| 대기열 진입/순번 조회 | 30ms | 80ms | 150ms | Redis 직접 처리 |
| 예매 확정 요청 | 100ms | 300ms | 500ms | Kafka 발행까지의 시간 |
| WebSocket 메시지 전파 | - | 200ms | 500ms | 좌석 상태 변경 브로드캐스트 |

### 1-2. 처리량 (Throughput)

| 시나리오 | 목표 TPS | 비고 |
| --- | --- | --- |
| 대기열 진입 (피크) | 5,000+ | 오픈 직후 동시 접속 |
| 좌석 점유 요청 (피크) | 1,000+ | 입장 허용된 사용자들의 동시 선택 |
| WebSocket 동시 연결 | 5,000+ | 좌석 선택 페이지 접속자 |

### 1-3. 부하 테스트 기준

모든 성능 요구사항은 k6 부하 테스트로 검증하며, 다음 조건을 충족해야 합니다:

- 에러율 1% 미만
- 좌석 초과 판매 0건 (정합성 절대 조건)
- 테스트 결과는 `docs/load-test/` 디렉토리에 날짜별로 기록
- 성능 개선 전후 비교 데이터를 반드시 함께 기록

---

## 2. 데이터 정합성

### 2-1. 절대 불변 규칙

다음 규칙은 어떤 상황에서도 위반되어서는 안 됩니다:

1. **좌석 초과 판매 금지**: 하나의 좌석은 하나의 예매에만 확정(RESERVED)될 수 있다
2. **점유 중 이중 할당 금지**: HELD 상태의 좌석은 점유자 외 다른 사용자가 점유할 수 없다
3. **결제 중복 처리 금지**: 동일 Idempotency Key에 대해 결제는 최대 1회만 처리된다
4. **대기열 순서 보장**: 먼저 진입한 사용자가 먼저 입장한다 (FIFO)

### 2-2. Redis-DB 정합성

Redis(임시 상태)와 MySQL(영구 상태) 간 불일치는 시스템 장애의 주요 원인입니다:

- **좌석 점유 순서**: Redis Lua Script로 원자적 점유 먼저 → DB 상태 업데이트 → DB 실패 시 Redis 보상(키 삭제)
- Redis TTL 만료 시 반드시 DB 상태를 동기화하는 핸들러가 동작해야 한다 (Keyspace Notification은 best-effort 보조, 정합성 배치가 주 메커니즘)
- 정합성 체크 배치를 1분 간격으로 실행하여, Redis에 키가 없는데 DB에 HELD인 좌석을 자동 복구한다
- Redis 장애 시 DB를 기준으로 복구하는 Fallback 전략을 구현한다
- Redis와 DB의 상태 불일치가 발생한 경우 WARN 레벨 로그를 남기고 모니터링 알림을 발송한다

### 2-3. 이벤트 처리 보장

- Kafka 메시지는 최소 1회 전달(at-least-once)을 보장한다
- Consumer는 반드시 멱등하게 구현하여 중복 메시지를 안전하게 처리한다
- 처리 실패 메시지는 Dead Letter Topic으로 이동하며, 재처리 메커니즘을 갖춘다
- 보상 트랜잭션(Saga) 실패 시 CRITICAL 레벨 로그 + 수동 개입 알림

---

## 3. 가용성 및 장애 대응

### 3-1. 장애 격리

- 결제 서비스 장애가 좌석 조회/선택 기능에 영향을 주지 않아야 한다
- Kafka 브로커 일시 장애 시 예매 요청은 로컬 버퍼에 보관 후 재전송한다
- 외부 의존성(Redis, Kafka, DB) 장애 시 Circuit Breaker를 적용한다

### 3-2. Graceful Degradation

| 장애 상황 | 대응 전략 |
| --- | --- |
| Redis 장애 | DB 직접 조회로 Fallback, 성능 저하 감수 (좌석 점유는 DB 비관적 락으로 전환) |
| Kafka 장애 | 예매 요청을 인메모리 큐에 버퍼링, Kafka 복구 시 재발행 |
| WebSocket 대량 끊김 | 클라이언트 폴링 Fallback, 재연결 시 전체 상태 동기화 |
| DB 커넥션 풀 고갈 | 대기열 입장 속도 자동 감소, 관리자 알림 |

### 3-3. 타임아웃 정책

| 구간 | 타임아웃 | 비고 |
| --- | --- | --- |
| Redis 명령 | 500ms | 초과 시 Fallback |
| DB 쿼리 | 3초 | 초과 시 쿼리 최적화 필요 |
| Kafka 발행 | 5초 | 초과 시 로컬 버퍼 저장 |
| 외부 결제 (모의) | 10초 | 초과 시 결제 실패 처리 |

---

## 3-4. Rate Limiting

API 보호를 위해 Redis Sliding Window Counter 기반 Rate Limiting을 적용한다:

| 엔드포인트 유형 | 제한 | 윈도우 | 초과 시 응답 |
| --- | --- | --- | --- |
| 좌석 점유 API | 사용자당 1회 | 10초 | 429 Too Many Requests |
| 대기열 진입 API | 사용자당 1회 | 1초 | 429 Too Many Requests |
| 일반 조회 API | 사용자당 10회 | 1초 | 429 Too Many Requests |

- Rate Limit 초과 시 `Retry-After` 헤더에 재시도 가능 시점을 포함한다
- JWT 파싱 후 사용자별 제한을 적용한다 (IP 기반이 아닌 userId 기반)
- 상세 설계는 ADR `0001-rate-limiting-strategy.md` 참조

### 3-5. Graceful Shutdown

애플리케이션 종료 시 진행 중인 작업을 안전하게 마무리한다:

- **종료 타임아웃**: 30초 (Spring Boot `server.shutdown=graceful`)
- **HTTP 요청**: 새 요청 수신 중단 → 진행 중 요청 완료 대기 → 타임아웃 시 강제 종료
- **Kafka Consumer**: 현재 처리 중인 메시지의 offset commit 후 중단
- **WebSocket**: 연결된 클라이언트에 재연결 메시지 전송 후 연결 종료
- **좌석 점유**: Redis TTL이 자연 만료하므로 shutdown 시 별도 처리 불필요
- 상세 설계는 ADR `0002-graceful-shutdown-strategy.md` 참조

### 3-6. Health Check

Spring Boot Actuator 기반 헬스 체크 엔드포인트를 제공한다:

| 엔드포인트 | 용도 | 체크 항목 |
| --- | --- | --- |
| `/actuator/health/liveness` | 앱 프로세스 생존 확인 | JVM 상태 |
| `/actuator/health/readiness` | 트래픽 수신 가능 여부 | DB, Redis, Kafka 연결 |

- Kubernetes 배포 시 liveness/readiness probe로 활용
- readiness가 DOWN이면 로드밸런서가 해당 인스턴스로 트래픽을 보내지 않음

---

## 4. 보안

### 4-1. 인증/인가

- JWT Access Token은 서명 검증 + 만료 시간 검증을 모두 수행한다
- 입장 토큰(Entry Token)은 발급 대상 사용자 + 이벤트 + 만료 시간을 페이로드에 포함하며, 타인의 토큰으로 좌석 API를 호출할 수 없다
- 관리자 API는 ADMIN 역할 검증을 Controller 레벨이 아닌 Security Filter 레벨에서 수행한다
- Refresh Token은 httpOnly, Secure, SameSite=Strict 쿠키로만 전송한다

### 4-2. 입력 검증

- 모든 API 입력은 Controller 레벨에서 Bean Validation으로 1차 검증한다
- 비즈니스 규칙 검증(좌석 수 제한, 점유 시간 등)은 Service 레벨에서 수행한다
- Path Variable, Query Parameter의 타입 불일치는 400 에러로 처리한다

### 4-3. 대기열 어뷰징 방지

- 동일 사용자의 중복 대기열 진입을 차단한다
- 입장 토큰은 1회성이며, 좌석 선택 페이지 진입 시 소모된다
- Rate Limiting: 동일 사용자 기준 좌석 점유 API는 10초에 1회로 제한한다

---

## 5. 코드 품질

### 5-1. 아키텍처 원칙

- **계층 분리**: Controller → Service → Repository 단방향 의존. Controller에 비즈니스 로직을 작성하지 않는다
- **도메인 중심**: Entity에 비즈니스 로직을 포함하되, 인프라(Redis, Kafka) 의존 코드는 Service 또는 Infrastructure 계층에 위치한다
- **인터페이스 추상화**: Redis, Kafka 등 인프라 접근은 인터페이스로 추상화하여 테스트 용이성을 확보한다
- **불변 객체 우선**: DTO, 이벤트 메시지 등 값 객체는 가능한 한 불변(record 또는 final 필드)으로 선언한다

### 5-2. 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 패키지 | 소문자, 도메인 단위 | `com.ticketaca.booking` |
| 클래스 | PascalCase, 역할 접미사 | `SeatHoldService`, `BookingEventListener` |
| 메서드 | camelCase, 동사로 시작 | `holdSeats()`, `releaseExpiredHolds()` |
| 상수 | UPPER_SNAKE_CASE | `MAX_HOLD_SEATS`, `HOLD_TTL_SECONDS` |
| REST API | kebab-case (URL), camelCase (JSON) | `/api/v1/events/{eventId}/seats/hold` |
| Kafka Topic | dot-notation | `ticketaca.booking.confirmed`, `ticketaca.payment.failed` |
| Redis Key | 콜론 구분, 계층적 | `hold:{eventId}:{seatId}`, `queue:{eventId}` |

### 5-3. 예외 처리

- 비즈니스 예외는 커스텀 Exception 클래스를 정의하고, 공통 에러 코드를 부여한다
- 예상치 못한 예외(NullPointerException 등)는 GlobalExceptionHandler에서 500으로 처리하되, 내부 스택트레이스를 응답에 노출하지 않는다
- 외부 시스템(Redis, Kafka, DB) 호출 실패는 반드시 try-catch로 감싸고, 실패 시 Fallback 또는 재시도 전략을 적용한다
- 에러 응답에는 반드시 고유한 에러 코드와 사용자 친화적 메시지를 포함한다

### 5-4. 로깅 기준

| 레벨 | 사용 기준 | 예시 |
| --- | --- | --- |
| ERROR | 시스템 동작에 영향을 주는 예외 | DB 연결 실패, Kafka 발행 실패 |
| WARN | 비정상이지만 자동 복구 가능 | Redis-DB 정합성 불일치, 재시도 성공 |
| INFO | 주요 비즈니스 이벤트 | 예매 확정, 좌석 점유, 대기열 입장 허용 |
| DEBUG | 개발/디버깅용 상세 정보 | 쿼리 파라미터, Redis 명령 상세 |

- 모든 로그에는 `traceId`를 포함하여 요청 단위 추적이 가능하도록 한다
- 개인정보(이메일, 비밀번호 등)는 로그에 절대 포함하지 않는다
- 성능 민감 구간(좌석 점유, 대기열 처리)은 실행 시간을 INFO 레벨로 기록한다

---

## 6. 테스트

### 6-1. 테스트 종류 및 커버리지

| 종류 | 대상 | 도구 | 최소 커버리지 |
| --- | --- | --- | --- |
| 단위 테스트 | Domain, Service 로직 | JUnit 5, Mockito | 핵심 비즈니스 로직 90% |
| 통합 테스트 | API 엔드포인트, Redis/DB 연동 | Testcontainers, MockMvc | 모든 API 엔드포인트 |
| 동시성 테스트 | 좌석 점유, 대기열 | CountDownLatch, ExecutorService | 핵심 동시성 시나리오 전체 |
| 부하 테스트 | 전 구간 | k6 | Phase별 시나리오 |

### 6-2. 필수 테스트 시나리오

다음 시나리오는 반드시 자동화된 테스트가 존재해야 합니다:

**동시성 관련**:
- 동일 좌석에 대한 N명 동시 점유 요청 → 정확히 1명만 성공
- 연석 선택 시 일부 좌석만 가용 → 전체 실패 (원자성)
- 점유 TTL 만료 → DB 상태 AVAILABLE 복구 확인
- 동일 사용자 대기열 중복 진입 → 기존 순번 반환

**보상 트랜잭션 관련**:
- 결제 실패 → 좌석 상태 AVAILABLE 복구
- 결제 타임아웃 → 좌석 상태 AVAILABLE 복구
- Kafka 메시지 중복 수신 → 멱등 처리 확인

**경계값 관련**:
- 최대 4석 선택 → 성공
- 5석 선택 → 실패
- 점유 만료 직전(4분 59초) 결제 요청 → 성공
- 점유 만료 직후(5분 1초) 결제 요청 → 실패

### 6-3. 테스트 격리

- 각 테스트는 독립적으로 실행 가능해야 한다 (순서 의존 금지)
- 통합 테스트는 Testcontainers로 격리된 Redis, MySQL 인스턴스를 사용한다
- 테스트 데이터는 @BeforeEach에서 생성하고 @AfterEach에서 정리한다

---

## 7. API 설계 원칙

- RESTful 원칙을 준수하되, 실시간 데이터는 WebSocket/SSE를 사용한다
- 모든 API 응답은 일관된 Envelope 형식(`status`, `data`, `error`)을 따른다
- 리스트 조회는 커서 기반 페이지네이션을 사용한다 (offset 기반 금지)
- API 버저닝은 URL Path 방식 (`/api/v1/`)을 사용한다
- 멱등한 작업(조회, 삭제)은 재시도에 안전하도록 설계한다
- 요청/응답 JSON 필드는 camelCase를 사용한다

---

## 8. 모니터링 및 관측성

### 8-1. 필수 메트릭

| 메트릭 | 수집 방식 | 알림 조건 |
| --- | --- | --- |
| API 응답 시간 (p50, p95, p99) | Micrometer + Prometheus | p99 > 1초 |
| API 에러율 | Micrometer + Prometheus | 5분간 에러율 > 5% |
| 대기열 크기 | Redis ZCARD + Custom Metric | 10,000명 초과 |
| 좌석 점유율 | Custom Metric | 이벤트별 95% 초과 (매진 임박) |
| Redis 응답 시간 | Lettuce Metrics | p99 > 100ms |
| DB 커넥션 풀 사용률 | HikariCP Metrics | 80% 초과 |
| Kafka Consumer Lag | Kafka Metrics | 1,000건 초과 |
| WebSocket 연결 수 | Custom Metric | 서버당 5,000 초과 |
| JVM Heap 사용률 | Micrometer | 85% 초과 |

### 8-2. 분산 추적

- 모든 HTTP 요청에 traceId를 부여하고, Kafka 메시지에도 전파한다
- 좌석 점유 → 예매 → 결제 → 알림 전체 흐름을 하나의 traceId로 추적 가능해야 한다
- 분산 추적 UI(Zipkin)에서 병목 구간을 시각적으로 확인할 수 있어야 한다

### 8-3. 대시보드

Grafana 대시보드는 최소 다음 패널을 포함한다:

- 실시간 TPS 및 응답 시간 그래프
- 대기열 크기 추이
- 구역별 좌석 판매율 게이지
- Redis/DB/Kafka 헬스 상태
- 에러율 추이 및 에러 코드별 분포

---

## 9. 문서화

### 9-1. 코드 문서화

- Public 메서드에는 JavaDoc을 작성한다 (파라미터, 반환값, 예외 설명 포함)
- 복잡한 비즈니스 로직에는 "왜 이렇게 구현했는지"를 주석으로 남긴다
- Redis Key 패턴, Kafka Topic 이름은 상수로 정의하고 JavaDoc에 용도를 명시한다

### 9-2. 의사결정 기록 (ADR)

기술적 의사결정은 `docs/adr/` 디렉토리에 Architecture Decision Record로 기록한다:

- 파일명: `NNNN-제목.md` (예: `0001-좌석-동시성-제어-방식-선정.md`)
- 내용 구성: 상황(Context) → 선택지 비교 → 결정(Decision) → 결과(Consequences)
- 부하 테스트 결과 등 정량적 근거를 반드시 포함한다

### 9-3. 트러블슈팅 기록

장애 또는 성능 이슈 해결 시 `docs/troubleshooting/` 디렉토리에 기록한다:

- 파일명: `YYYY-MM-DD-제목.md`
- 내용 구성: 증상 → 원인 분석 → 시도한 방법들 → 최종 해결 → 학습한 점
- 모니터링 스크린샷, 부하 테스트 전후 비교 데이터를 포함한다

---

## 10. Git 컨벤션

### 10-1. 브랜치 전략

```
main ─── 배포 가능한 상태만 유지
 └── develop ─── 개발 통합 브랜치
      ├── feat/{기능명} ─── 기능 개발
      ├── fix/{버그명} ─── 버그 수정
      ├── refactor/{대상} ─── 리팩토링
      ├── perf/{대상} ─── 성능 개선
      ├── test/{대상} ─── 테스트 추가
      └── docs/{문서명} ─── 문서 작성
```

### 10-2. 커밋 메시지

```
<type>(<scope>): <subject>

<body>

<footer>
```

| type | 설명 |
| --- | --- |
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링 (기능 변경 없음) |
| perf | 성능 개선 |
| test | 테스트 추가/수정 |
| docs | 문서 변경 |
| chore | 빌드, 설정 변경 |

### 10-3. PR 규칙

- PR 제목은 커밋 메시지 형식을 따른다
- PR 본문에는 변경 사항 요약, 테스트 결과, 관련 이슈 번호를 포함한다
- 자기 자신의 PR이라도 코드를 다시 읽고 셀프 리뷰를 수행한 후 머지한다
- CI(테스트, 린트)가 통과한 PR만 머지 가능하다
