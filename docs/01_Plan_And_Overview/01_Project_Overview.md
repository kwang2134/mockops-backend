# 기획서

## 📝 MockOps 프로젝트 상세 최종 기획서

MockOps는 프로젝트 단위의 팀 협업 도구로, 개발 초기 Mock API 제공과 도메인 서버의 상태를 통합 관리하는 대시보드 서비스를 제공합니다. **Mocking과** **주기적 헬스 체크를 통한 모니터링 기능**을 핵심으로 합니다.

---

### 1. 🎯 프로젝트 개요 및 핵심 기능

| **항목** | **내용** |
| --- | --- |
| **서비스명** | **MockOps** (Mocking & Operational Status Dashboard) |
| **목표** | 백엔드 개발 병목 해소 및 다중 도메인 서버 상태의 통합 가시성 제공. |
| **인증** | 회원제 기반, **JWT**를 사용한 자체 인증 및 **OAuth 2.0** (Google, Kakao, Naver, GitHub) 지원. |
| **핵심 기능** | Mock API 동적 제공, 프로젝트/팀 관리, **도메인 상태 대시보드**, **주기적 헬스 체크**, **웹 훅 기반 상태 변경 및 인증**, **알림 연동**. |

---

### 2. 🗄️ 데이터 모델 설계 (Database Schema)

| **엔티티** | **필드명 (PK/FK)** | **타입** | **설명** |
| --- | --- | --- | --- |
| **`BaseEntity`** | `createdAt` | Instant | 생성 일시 (UTC) |
|  | `updatedAt` | Instant | 최종 수정 일시 (UTC) |
| --- | --- | --- | --- |
| **`User`** | `id` (PK) | Long | 사용자 고유 ID |
|  | `email` | String | 로그인 이메일 (유니크) |
|  | `nickname` | String | 닉네임 |
|  | `role` | Enum | 권한 (`USER`, `ADMIN`) |
| **`AuthProvider`** | `id` (PK) | Long | OAuth 제공자 고유 ID |
|  | `userId` (FK) | Long | **연결된 사용자 ID** |
|  | `providerType` | Enum | 제공자 (`GOOGLE`, `KAKAO`, `NAVER`, `GITHUB`) |
|  | `providerId` | String | 제공자별 사용자 고유 ID |
|  | `refreshToken` | String | **OAuth 리프레시 토큰** |
| **`Project`** | `id` (PK) | Long | 프로젝트 고유 ID |
|  | `name` | String | 프로젝트 이름 (유니크) |
|  | `description` | String | 프로젝트 설명 |
|  | `ownerId` (FK) | Long | **프로젝트 생성자 ID** |
|  | `slackWebhookUrl` | String | 알림용 슬랙 웹훅 URL |
| **`ProjectMember`** | `id` (PK) | Long | 프로젝트 팀원 정보 |
|  | `projectId` (FK) | Long | **소속 프로젝트 ID** |
|  | `userId` (FK) | Long | **팀원 사용자 ID** |
|  | `memberRole` | Enum | 프로젝트 내 역할 (`OWNER`, `MANAGER` `DEVELOPER`, `VIEWER`) |
| **`ProjectCorsOrigin`** | `id` (PK) | Long | CORS 허용 오리진 고유 ID |
|  | `projectId` (FK) | Long | 소속 프로젝트 ID |
|  | `originUrl` | String | 허용할 Origin URL |
| **`DomainServer`** | `id` (PK) | Long | 도메인 서버 고유 ID |
|  | `projectId` (FK) | Long | 소속 프로젝트 ID |
|  | `name` | String | 도메인 서버 이름 |
|  | `status` | Enum | 현재 개발 상태 (`MOCKING`, `DEPLOYED`, `ERROR` 등) |
|  | `healthCheckUrl` | String | 실제 서버의 헬스 체크 API URL |
|  | `healthCheckInterval` | String | 헬스 체크 주기 (5분, 10분, 30분, 1시간) 미선택 시 기본 10분 |
|  | `lastCheckedAt` | Instant | 최종 헬스 체크 일시 |
| **`MockApi`** | `id` (PK) | Long | Mock API 엔드포인트 정보 |
|  | `serverId` (FK) | Long | 소속 도메인 서버 ID |
|  | `name`  | String | API 라벨/이름 |
|  | `httpMethod` | Enum | HTTP 메서드 |
|  | `endpointPath` | String | 엔드포인트 경로 |
|  | `responseBody` | Text | Mock 응답 JSON 데이터 |
|  | `statusCode` | Integer | HTTP 응답 상태 코드 |
|  | `isActive` | Boolean | Mock API 활성화 여부 |
| **`WebhookSecret`** | `id` (PK) | Long | 웹 훅 인증 비밀 정보 |
|  | `projectId` (FK) | Long | 연결된 프로젝트 ID (유니크) |
|  | `secretKey` | String | **JWT 서명/검증에 사용되는 UUID 키 (암호화 됨)** |
|  | `isActive` | Boolean | **키 활성화 여부** (즉시 폐기 스위치) |
| **`Invitation`**  | `id` (PK) | Long | 초대 고유 ID |
|  | `projectId` (FK) | Long | 초대된 프로젝트 ID |
|  | `inviterId` (FK) | Long | 초대한 사용자 ID |
|  | `invitedEmail` | String | 초대 대상 이메일 |
|  | `tokenValue` | String | 발급된 JWT 토큰 값 (또는 해시) |
|  | `expiresAt` | Instant | 토큰 만료 시간 |
|  | `status` | Enum | 초대 상태 (PENDING, ACCEPTED, EXPIRED) |

---

### 3. ⚙️ 핵심 기능 상세 설계

### 3.1. 인증 및 권한 관리

1. **JWT 인증:** 사용자 로그인 성공 시 MockOps **자체 JWT**를 발급하며, 모든 관리 API (`/api/v1/`) 요청 시 이 토큰을 **`Authorization` 헤더**로 검증합니다.
2. **OAuth 2.0:** Google, Kakao, Naver, GitHub 소셜 로그인을 지원하며, 인증 후 `User` 및 `AuthProvider` 테이블에 정보를 저장합니다.

### 3.2. Mock API 동적 라우팅 및 제공

- **Endpoint:** `mockops.com/mock/{projectName}/**`
- **라우팅:** 요청 URL에서 `{projectName}`과 경로를 파싱하여 DB(`MockApi`)에 따라 동적으로 응답을 반환합니다.
- **파일 업로드:** YAML/JSON 파일을 업로드하면 `MockApi` 목록을 **자동으로 파싱하여 등록**하는 기능을 제공합니다.

### 3.3. CORS 동적 처리 및 캐싱 (강화)

- **저장 메커니즘:** 사용자가 프로젝트 설정에서 등록한 허용 Origin URL은 **`ProjectCorsOrigin`** 테이블에 저장됩니다.
- **로드 및 캐싱 로직:**
    1. Mock API 요청이 들어오면 **프로젝트 ID**를 식별합니다.
    2. **Redis**를 사용하여 해당 프로젝트 ID의 Origin 목록을 조회합니다.
    3. Cache Miss 시, **`ProjectCorsOrigin`** 테이블에서 모든 `origin_url`을 DB에서 조회 후 캐시에 저장하고 CORS 검증에 사용합니다.
    4. 설정 변경 시 캐시를 **명시적으로 제거**하여 즉시 최신 목록을 반영합니다.

### 3.4. 상태 변경 및 헬스 체크 등록 (WebHook Endpoint)

- **WebHook Endpoint:** `POST /api/v1/webhook/deployment/{projectName}`
- **웹 훅 인증:**
    1. WebHook 요청 시 **`WebhookSecret`*에 저장된 키를 사용하여 요청 헤더를 통해 인증 토큰을 전달해야 합니다.
    2. MockOps는 `{projectName}`에 해당하는 `secret_key`를 DB에서 조회하여 **요청의 유효성을 검증**합니다.
- **처리 로직:** 인증된 요청에 한해, `DomainServer`의 `status`를 `DEPLOYED`로 업데이트하고, 페이로드에 포함된 **`health_check_url`*을 저장합니다.

### 3.5. 주기적 헬스 체크 모니터링 (Scheduler)

1. **스케줄링:** Spring Boot **`@Scheduled`**를 사용하여 5분/10분/30분/1시간 간격으로 모니터링 작업을 실행합니다.
2. **대상 조회:** `status`가 `DEPLOYED`인 모든 `DomainServer`를 조회합니다.
3. **체크:** 등록된 **`health_check_url`**로 HTTP GET 요청을 보내 응답 상태를 확인합니다.
4. **상태 변경 및 알림:**
    - **Success (200 OK):** 상태 유지.
    - **Failure (4xx/5xx 또는 Timeout):** `DomainServer.status`를 **`ERROR`*로 변경하고, 설정된 **Slack WebHook**으로 알림 메시지를 즉시 전송합니다.

---

### 4. 🚀 개발 편의성 기능

- **대시보드:** 프로젝트 페이지에서 각 `DomainServer`의 **실시간 `status`** (`MOCKING`, `DEPLOYED`, `ERROR`)를 한눈에 확인할 수 있도록 표시합니다.