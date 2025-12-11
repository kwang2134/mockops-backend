# API 명세

## 🚀 MockOps 핵심 API 명세서

### 1. 🔑 인증 및 사용자 관리 (Auth & User)

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `GET` | `/api/v1/auth/{provider}/login` | OAuth 로그인 시작 | `Public` | None | None | 302 Redirect |
| **2** | `GET` | `/api/v1/auth/token/refresh` | Access Token 갱신 (Refresh Token은 HttpOnly Cookie로만 응답) | `REFRESH_TOKEN` | None | None | `AccessTokenResponse` |
| **3** | `GET` | `/api/v1/users/me` | 사용자 본인 정보 조회 | `MEMBER` | None | None | `UserDetailResponse` |
| **4** | `PATCH` | `/api/v1/users/me/nickname` | 닉네임 수정 | `MEMBER` | None | `UserUpdateNicknameRequest` | `UserDetailResponse` |
| **5** | `POST`  | `/api/v1/auth/logout` | 로그아웃 | `MEMBER` | None | None | 204 No Content |

---

### 2. 📁 프로젝트 관리 (Project)

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `POST` | `/api/v1/projects` | 프로젝트 생성 | `MEMBER` | None | `ProjectCreateRequest` | `ProjectCreateResponse` |
| **2** | `GET` | `/api/v1/projects` | 내 프로젝트 목록 조회 (페이지 기반 offset 페이징) | `MEMBER` | `page` (페이지  번호), `size` (페이지당 개수, 기본 10개) | None | `ProjectPageResponse` |
| **3** | `GET` | `/api/v1/projects/{projectId}` | 프로젝트 상세 조회 (현재 유저의 권한 포함) | `PROJECT_MEMBER` | None | None | `ProjectDetailResponse` |
| **4** | `PATCH` | `/api/v1/projects/{projectId}` | 프로젝트 정보 수정 | `PROJECT_OWNER` | None | `ProjectUpdateRequest` | `ProjectUpdateResponse` |
| **5** | `DELETE` | `/api/v1/projects/{projectId}` | 프로젝트 삭제 | `PROJECT_OWNER` | None | None | 204 No Content |

---

### 3. 👥 프로젝트 멤버 관리 (Project Member)

**Base URL:** `/api/v1/projects/{projectId}/members`

| **#** | **HTTP Method** | **URL** | **설명**                        | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- |-------------------------------| --- | --- | --- | --- |
| **1** | `POST` | `/members` | 팀원 초대 수락                      | `MEMBER` | None | `MemberInviteAcceptRequest` | `ProjectMemberResponse` |
| **2** | `GET` | `/members` | 프로젝트 팀원 목록 조회 (offset 기반 페이징) | `PROJECT_MEMBER` | `size`, `offset` | None | `MemberListResponse` |
| **3** | `PATCH` | `/members/{memberId}/role` | 팀원 역할 변경 (OWNER 제외)           | `PROJECT_MANGER` | None | `MemberRoleUpdateRequest` | `ProjectMemberResponse` |
| **4** | `DELETE` | `/members/{memberId}` | 팀원 제외 (OWNER 제외)              | `PROJECT_MANAGER` | None | None | 204 No Content |

---

### 4. 🌐 CORS Origin 관리 (CORS)

**Base URL:** `/api/v1/projects/{projectId}/cors`

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `POST` | `/cors` | 허용 Origin 추가 | `PROJECT_DEVELOPER` | None | `CorsOriginRequest` | `CorsOriginResponse` |
| **2** | `GET` | `/cors` | 허용 Origin 목록 조회 | `PROJECT_MEMBER` | None | None | `List<CorsOriginResponse>` |
| **3** | `DELETE` | `/cors/{corsId}` | 허용 Origin 삭제 | `PROJECT_DEVELOPER` | None | None | 204 No Content |

---

### 5. 🖥️ 도메인 서버 관리 (DomainServer)

**Base URL:** `/api/v1/projects/{projectId}/servers`

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `POST` | `/servers` | 서버 등록 (Mocking 상태로 생성) | `PROJECT_MANAGER` | None | `DomainServerCreateRequest` | `DomainServerCreateResponse` |
| **2** | `GET` | `/servers` | 서버 목록 조회 (페이지 기반) | `PROJECT_MEMBER` | `page` (페이지  번호), `size` (페이지당 개수, 기본 10개) | None | `DomainServerPageResponse` |
| **3** | `PATCH` | `/servers/{serverId}` | 서버 정보 및 상태 수동 수정 | `PROJECT_DEVELOPER` | None | `DomainServerUpdateRequest` | `DomainServerUpdateResponse` |
| **4** | `DELETE` | `/servers/{serverId}` | 서버 삭제 | `PROJECT_MANAGER` | None | None | 204 No Content |

---

### 6. 🖼️ Mock API 관리 (MockApi)

**Base URL:** `/api/v1/projects/{projectId}/servers/{serverId}/mocks`

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터**                    | **요청 DTO** | **응답 DTO**              |
| --- | --- | --- | --- | --- |--------------------------------| --- |-------------------------|
| **1** | `POST` | `/mocks` | Mock API 생성 (개별) | `PROJECT_DEVELOPER` | None                           | `MockApiCreateRequest` | `MockApiCreateResponse`       |
| **2** | `POST` | `/mocks/upload` | YAML 파일 업로드 및 일괄 처리 | `PROJECT_DEVELOPER` | None                           | `MultipartFile` | `MockApiBulkResponse`   |
| **3** | `GET` | `/mocks` | Mock API 목록 조회 (커서 기반) | `PROJECT_MEMBER` | `size`, `cursorId`, `cursorName` | None | `MockApiGroupResponse`  |
| **4** | `GET` | `/mocks/{mockId}` | Mock API 상세 조회 | `PROJECT_MEMBER` | None                           | None | `MockApiDetailResponse` |
| **5** | `PATCH` | `/mocks/{mockId}` | Mock API 수정 | `PROJECT_DEVELOPER` | None                           | `MockApiUpdateRequest` | `MockApiUpdateResponse`       |
| **6** | `DELETE` | `/mocks/{mockId}` | Mock API 삭제 | `PROJECT_DEVELOPER` | None                           | None | 204 No Content          |
| **7** | `GET` | `/api/v1/servers/{serverId}/mock-apis/active-job` | 서버의 진행 중인 Job 조회 | `PROJECT_DEVELOPER` | None                           | None | `JobStatusResponse` |

---

### 7. 🔒 Webhook Secret 관리 (Webhook Secret)

**Base URL:** `/api/v1/projects/{projectId}/webhook`

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `GET` | `/secret` | Secret Key 정보 조회 (키 값은 숨김) | `PROJECT_OWNER` | None | None | `WebhookSecretResponse` |
| **2** | `POST` | `/reissue` | Secret Key 재발급 (기존 키 즉시 폐기) | `PROJECT_OWNER` | None | None | `WebhookSecretResponse` |
| **3** | `PATCH` | `/deactivate` | Secret Key 비활성화 (`isActive=false`) | `PROJECT_OWNER` | None | None | `WebhookSecretResponse` |
| **4** | `POST` | `/token` | 30일 유효 기간Webhook JWT 발급 | `PROJECT_OWNER` | None | `WebhookTokenIssueRequest` | `WebhookTokenResponse` |

---

### 8. 🎯 Public/Infra 엔드포인트 (Mocking & WebHook 수신)

| **#** | **HTTP Method** | **URL**                             | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- |-------------------------------------| --- | --- | --- | --- | --- |
| **1** | `POST` | `/api/webhook/deploy/{projectId}`   | **CI/CD 배포 완료 WebHook 수신** | `WEBHOOK_JWT` | None | `DeploymentEventRequest` | 202 Accepted |
| **2** | `*` | `/mock/{projectId}/{serverSlug}/**` | Mock API 요청 수신 | `CORS/Public` | None | Client Request | Mock API 응답 |

---

### 9. 프로젝트 초대 관리 엔드포인트 (Project Invitation Management)

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가** | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **1** | `POST` | `/api/v1/projects/{projectId}/invitations` | **팀원 초대 생성 및 발송.** 초대 대상 이메일로 JWT 기반 초대 링크를 생성하고 이메일을 발송합니다. | `PROJECT_MANAGER` | - | `InvitationCreateRequest` | `InvitationCreateResponse` |
| **2** | `GET` | `/api/v1/projects/{projectId}/invitations` | **진행 중인 초대 목록 조회.** 해당 프로젝트의 수락 대기(`PENDING`) 중인 초대장 목록을 조회합니다. | `PROJECT_MANAGER` | **`page`** (Integer, Optional), **`size`** (Integer, Optional), `status` (String, Optional) | - | `PagedInvitationListResponse` |
| **3** | `DELETE` | `/api/v1/projects/{projectId}/invitations/{invitationId}` | **초대 취소.** 수락 대기 중인 초대장을 취소(`CANCELED` 상태로 변경)합니다. | `PROJECT_MANAGER` | - | - | 204 No Content |
| **4** | `GET` | `/public/invitations/accept` | **초대 수락 처리.** 초대 이메일 링크 클릭 시 호출. URL 파라미터로 전달된 **JWT 토큰의 유효성을 검증**하고, 성공 시 `ProjectMember`를 생성합니다. (로그인 필요 시 리다이렉트 처리) | `NONE` (토큰 유효성 검증) | `token` (String, Required) | - | 302 Redirect |

---

### 10. Job Tracking 엔드포인트 (비동기 작업 추적)

| **#** | **HTTP Method** | **URL** | **설명** | **인증/인가**           | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
| --- |-----------------| --- | --- |---------------------| --- | --- | --- |
| **1** | `GET`           | `/api/v1/jobs/{jobId}` | 비동기 벌크 작업 상태 조회 (Polling) | `PROJECT_DEVELOPER` | None | None | `JobStatusResponse` |

---

### 11. 알림 관리 엔드포인트

| **#** | **HTTP Method** | **URL**                                                         | **설명**                                                                    | **인증/인가**       | **쿼리 파라미터** | **요청 DTO** | **응답 DTO** |
|-------|-----------------|-----------------------------------------------------------------|---------------------------------------------------------------------------|-----------------| --- |------------| --- |
| **1** | `GET`           | `/api/v1/notifications/user`                                    | **[사용자 알림]** 현재 사용자에게 귀속된 알림 목록을 최신순으로 조회. (초대장, 시스템 공지 등)                | `AUTHENTICATED` | `cursorId` (Long), `pageSize` (Int) | None       | `UserNotificationPageResponse` |
| **2** | `PATCH`         | `/api/v1/notifications/{notificationId}/read`                   | **[알림 처리]** 특정 알림을 '읽음' 상태로 변경합니다.                                        | `AUTHENTICATED` | None | None       | None (204 No Content) |
| **3** | `DELETE`        | `/api/v1/notifications/{notificationId}`                        | **[알림 삭제]** 특정 알림을 삭제합니다. (사용자 귀속 알림은 해당 사용자만 삭제 가능하며, 서버 알림은 프로젝트 멤버 권한 확인 필요)                                        | `AUTHENTICATED` | None | None       | None (204 No Content) |
| **4** | `GET`           | `/api/v1/notifications/server/{serverId}`                       | **[서버 알림 요약]** 특정 도메인 서버에 귀속된 알림을 타입별로 최근 10개씩 요약 조회합니다.        | `PROJECT_MEMBER`  |None| None       | `ServerNotificationSummaryResponse` |
| **5** | `GET`           | `/api/v1/notifications/server/{serverId}/health-check-failures` | **[헬스 체크 실패 로그]** 특정 서버의 `HEALTH_CHECK_FAILURE` 알림만 최신순으로 상세 조회합니다. (시간대별 실패 기록 로그 대용)       | `PROJECT_MEMBER`  |`cursorId` (Long), `pageSize` (Int)| None       | `HealthCheckFailureLogPageResponse` |

