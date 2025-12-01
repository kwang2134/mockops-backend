# tech_req_webhook_ops

## 🔒 WebHook 보안 및 운영 기술 요구사항

### 1. 🛡️ WebHook JWT 및 Secret Key 관리

| **구분** | **요구사항** | **상세 정책 및 구현 방안** |
| --- | --- | --- |
| **Secret Key 저장소** | **`WebhookSecret` 엔티티** | `WebhookSecret` 엔티티의 `secretKey` 필드에 저장합니다. |
| **Secret Key 저장 방식** | **양방향 암호화 (AES-256 GCM)** | JWT 서명 검증을 위해 복호화가 필수이므로, **AES-256 (GCM Mode)**를 사용하여 Secret Key를 암호화하여 DB에 저장합니다. 암호화 키는 환경 변수 또는 Vault에 보관합니다. |
| **WebHook JWT 수명 (TTL)** | **30일** | CI/CD 파이프라인의 편의성을 위해 30일로 설정합니다. |
| **WebHook 검증 방식** | **`projectId` 기반 검증** | 1. WebHook 요청에서 `projectId`를 추출합니다. 2. 해당 `projectId`로 DB/Cache에서 **암호화된 Secret Key**를 조회합니다. 3. Secret Key를 복호화하여 JWT의 서명을 검증합니다. |
| **WebHook 응답** | **202 Accepted** | WebHook 요청 수신 시, 응답을 빠르게 반환하여 CI/CD 파이프라인의 지연을 최소화합니다. 실제 상태 변경 로직은 비동기로 처리됩니다. |

---

### 2. 🩺 도메인 서버 헬스 체크 구현 (벌크 스케줄링)

MockOps의 성능과 확장성을 위해 **고정된 주기의 벌크 스케줄러**와 **Redis**를 활용합니다.

### A. 스케줄링 및 주기 정책

| **항목** | **요구사항** | **정책 및 구현 방안** |
| --- | --- | --- |
| **헬스 체크 주체** | **Spring `@Scheduled` 기반 다중 스케줄러** | **5분, 10분, 30분, 1시간** 간격으로 동작하는 고정된 `@Scheduled` 메서드를 각각 구현합니다. |
| **활성화 제어** | **명시적 `isHealthCheckActive` 플래그 사용** | `DomainServer` 엔티티에 `isHealthCheckActive` 플래그를 두어 **URL 존재 유무와 관계없이** 스케줄러 작동 여부를 제어합니다. |
| **기본 주기** | **10분** | 사용자가 주기를 설정하지 않을 경우의 기본값으로 `DomainServer` 엔티티에 저장됩니다. |
| **오류 임계치** | **3회 연속 실패** | 연속된 3회 헬스 체크 실패 시 `DomainServer`의 `status`를 **`ERROR`**로 변경합니다. |

### B. Redis 기반 벌크 작업

| **항목** | **요구사항** | **상세 구현 방안**                                                                                                                                    |
| --- | --- |-------------------------------------------------------------------------------------------------------------------------------------------------|
| **스케줄링 목록 저장소** | **Redis Set** | 주기별 서버 ID 및 URL 리스트를 Redis SET에 저장하여 분산 환경에서 일관성을 유지하고 DB 부하를 제거합니다                                                                             |
| **Redis Key 구조** | `healthcheck:jobs:{interval}` | 예: `healthcheck:jobs:5m`, `healthcheck:jobs:10m`, `healthcheck:jobs:30m`, `healthcheck:jobs:1h`                                                  |
| **Redis Value 구조** | `[DomainServerId]:[HealthCheckPath]` (문자열) | 스케줄러가 **DB 접근 없이** 즉시 헬스 체크를 수행할 수 있도록 필요한 URL 정보를 함께 저장합니다. 예: `"123:/api/health" `                                                                                   |
| **데이터 TTL** | **TTL 미사용 (명시적 관리)** | TTL을 설정하지 않으며, `DomainServer` 설정 변경 시 서비스 로직(`HealthCheckService`)에서 `SADD`/`SREM` 명령을 통해 원자적으로(Atomic) 데이터를 직접 관리합니다. |
| **데이터 동기화** | **Atomic Update(플래그 기반)** | `isHealthCheckActive`**가 true일 때만** 해당 주기의 Redis Set에 `SADD`로 추가합니다. 플래그가 **false로 변경되면** 기존 주기의 Redis Set에서 `SREM`으로 제거 작업을 수행합니다. |

---

### 3. 📢 운영 알림 (Slack 연동)

| **구분** | **요구사항** | **상세 정책** |
| --- | --- | --- |
| **알림 발생 시점** | **`DEPLOYED` →`ERROR`** (장애 발생) 및 **`ERROR` → `DEPLOYED`** (장애 복구) 시점 |  |
| **알림 내용** | **프로젝트 이름, 서버 이름, 이전 상태 → 신규 상태, 변경 일시** | `Project` 엔티티에 저장된 Slack WebHook URL을 사용하여 알림을 전송합니다. |