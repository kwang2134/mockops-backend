# DTO 명세

## 📄 dto_specification.md: 핵심 DTO 명세서

### 1. 🔑 인증 및 사용자 관리 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명** |
| --- | --- | --- | --- | --- |
| **`TokenResponse`** | Access/Refresh 토큰 응답 | `accessToken` | String | 실제 API 호출에 사용되는 JWT |
|  |  | `refreshToken` | String | Access Token 갱신에 사용되는 토큰 |
| **`UserDetailResponse`** | 사용자 정보 응답 | `id` | Long | 사용자 고유 ID |
|  |  | `email` | String | 사용자 이메일 |
|  |  | `nickname` | String | 사용자 닉네임 |
|  |  | `role` | String | 사용자 권한 (`USER`/`ADMIN`) |
| **`UserUpdateNicknameRequest`** | 닉네임 변경 요청 | `nickname` | String | 변경할 새로운 닉네임 |

---

### 2. 📁 프로젝트 관리 DTO

| **DTO 클래스명**                | **역할**                     | **필드명**           | **타입**                  | **설명**                      |
|-----------------------------|----------------------------|-------------------|-------------------------|-----------------------------|
| **`ProjectCreateRequest`**  | 프로젝트 생성 요청                 | `name`            | String                  | 프로젝트 이름                     |
|                             |                            | `description`     | String                  | 프로젝트 설명 (선택 사항)             |
|                             |                            | `slackWebhookUrl` | String                  | 상태 알림을 받을 슬랙 웹훅 URL (선택 사항) |
| **`ProjectUpdateRequest`**  | 프로젝트 정보 수정 요청              | `description`     | String                  | 수정할 프로젝트 설명                 |
|                             |                            | `slackWebhookUrl` | String                  | 수정할 슬랙 웹훅 URL               |
| **`ProjectCreateResponse`** | 프로젝트 생성 성공 응답              | `id`              | Long                    | 생성된 프로젝트 ID                 |
|                             |                            | `name`            | String                  | 프로젝트 이름                     |
|                             |                            | `createdAt`       | Instant                 | 생성 일시                       |
| **`ProjectUpdateResponse`** | 프로젝트 정보 수정 성공 응답           | `id`              | Long                    | 수정된 프로젝트 ID                 |
|                             |                            | `updatedAt`       | Instant                 | 최종 수정 일시                    |
| **`ProjectDetailResponse`** | 프로젝트 상세 정보 응답              | `id`              | Long                    | 프로젝트 고유 ID                  |
|                             |                            | `name`            | String                  | 프로젝트 이름                     |
|                             |                            | `description`     | String                  | 프로젝트 설명                     |
|                             |                            | `ownerId`         | Long                    | 생성자 사용자 ID                  |
|                             |                            | `ownerNickName`   | String                  | 생성자(PO) 닉네임                 |
|                             |                            | `slackWebhookUrl` | String                  | 슬랙 웹훅 URL                   |
|                             |                            | `createdAt`       | Instant                 | 생성 일시                       |
|                             |                            | `updatedAt`       | Instant                 | 최종 수정 일시                    |
| **`ProjectResponse`**       | 프로젝트 목록 개별 응답              | `id`              | Long                    | 프로젝트 고유 ID                  |
|                             |                            | `name`            | String                  | 프로젝트 이름                     |
|                             |                            | `description`     | String                  | 설명                          |
|                             |                            | `ownerNickName`   | String                  | 생성자(PO) 닉네임                 |
|                             |                            | `updatedAt`       | Instant                 | 최종 수정 일시                    |
| **`ProjectPageResponse`**   | 프로젝트 목록 응답 (페이지 기반 offset) | `data`            | List<`ProjectResponse`> | 프로젝트 목록 정보 리스트              |
|                             |                            | `totalPages`      | Integer                 | 전체 페이지 수                    |
|                             |                            | `totalElements`   | Long                    | 전체 프로젝트 개수                  |
|                             |                            | `currentPage`     | Integer                 | 현재 페이지 번호 (0부터 시작)          |

---

### 3. 👥 프로젝트 멤버 관리 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명** |
| --- | --- | --- | --- | --- |
| **`MemberInviteRequest`** | 멤버 초대 요청 | `email` | String | 초대할 사용자의 이메일 |
| **`MemberRoleUpdateRequest`** | 멤버 역할 변경 요청 | `memberRole` | String | 변경할 역할 (`OWNER`/`MANAGER`/`DEVELOPER`/`VIEWER`) |
| **`ProjectMemberResponse`** | 멤버 정보 응답 | `id` | Long | `ProjectMember` 고유 ID |
|  |  | `userId` | Long | 사용자 ID |
|  |  | `nickname` | String | 사용자 닉네임 |
|  |  | `memberRole` | String | 프로젝트 내 역할 |
| **`MemberListResponse`** | 멤버 목록 응답 | `members` | List<`ProjectMemberResponse`> | 페이징된 멤버 정보 리스트 |
|  |  | `hasNext`  | Boolean | 다음 페이지 존재 여부 (커서 기반 페이징) |
|  |  | `nextCursorId`  | Long | 다음 페이지 조회를 위한 커서 ID (마지막 멤버 ID) |

---

### 4. 🌐 CORS Origin 관리 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명** |
| --- | --- | --- | --- | --- |
| **`CorsOriginRequest`** | Origin 추가 요청 | `originUrl` | String | 허용할 Origin URL |
| **`CorsOriginResponse`** | Origin 정보 응답 | `id` | Long | `ProjectCorsOrigin` 고유 ID |
|  |  | `originUrl` | String | 허용된 Origin URL |

---

### 5. 🖥️ 도메인 서버 관리 DTO

| **DTO 클래스명**                     | **역할**      | **필드명**               | **타입**                             | **설명**                                 |
|----------------------------------|-------------|-----------------------|------------------------------------|----------------------------------------|
| **`DomainServerCreateRequest`**  | 서버 생성 요청    | `name`                | String                             | 서버 이름                                  |
|                                  |             | `slug`                | String                             | mock 요청 URL에 사용될 값                     |
| **`DomainServerUpdateRequest`**  | 서버 정보 수정 요청 | `name`                | String                             | 수정할 서버 이름                              |
|                                  |             | `healthCheckUrl`      | String                             | 수정할 헬스 체크 URL (수동 입력)                  |
|                                  |             | `healthCheckInterval` | String                             | 수정할 헬스 체크 주기 (기본 10분)                  |
|                                  |             | `isHealthCheckActive` | Boolean                             | 수정할 헬스 체크 활성화 여부                       |
|                                  |             | `status`              | String                             | 수동으로 변경할 서버 상태                         |
| **`DomainServerCreateResponse`** | 서버 생성 성공 응답 | `id`                  | Long                               | 생성된 서버 ID                              |
|                                  |             | `name`                | String                             | 생성된 서버 이름                              |
|                                  |             | `createdAt`           | Instant                            | 생성 일시                                  |
| **`DomainServerUpdateResponse`** | 서버 수정 성공 응답 | `id`                  | Long                               | 수정된 서버 ID                              |
|                                  |             | `name`                | String                             | 수정된 서버 이름                              |
|                                  |             | `status`              | String                             | 수정된 서버 상태                              |
|                                  |             | `healthCheckUrl`      | String                             | 수정된 헬스 체크 URL                          |
|                                  |             | `healthCheckInterval` | String                             | 수정된 헬스 체크 주기                           |
|                                  |             | `isHealthCheckActive` | Boolean                             | 수정된 헬스 체크 활성화 여부                       |
|                                  |             | `updatedAt`           | Instant                            | 최종 수정 일시                               |
| **`DomainServerResponse`**       | 서버 상세 정보 응답 | `id`                  | Long                               | 서버 고유 ID                               |
|                                  |             | `name`                | String                             | 서버 이름                                  |
|                                  |             | `status`              | String                             | 현재 상태 (`MOCKING`/`DEPLOYED`/`ERROR` 등) |
|                                  |             | `healthCheckUrl`      | String                             | 헬스 체크 URL                              |
|                                  |             | `healthCheckInterval` | String                             | 헬스 체크 주기                               |
|                                  |             | `lastCheckedAt`       | Instant                            | 최종 헬스 체크 일시                            |
|                                  |             | `isHealthCheckActive` | Boolean                             | 헬스 체크 활성화 여부                           |
|                                  |             | `createdAt`           | Instant                            | 생성 일시                                  |
|                                  |             | `updatedAt`           | Instant                            | 최종 수정 일시                               |
| **`DomainServerSimpleResponse`** | 서버 목록 개별 응답 | `id`                  | Long                               | 서버 고유 ID                               |
|                                  |             | `name`                | String                             | 서버 이름                                  |
|                                  |             | `status`              | String                             | 현재 상태 (`MOCKING`/`DEPLOYED`/`ERROR` 등) |
|                                  |             | `updatedAt`           | Instant                            | 최종 수정 일시                               |
| **`DomainServerListResponse`**   | 서버 목록 응답    | `data`                | List<`DomainServerSimpleResponse`> | 페이지에 해당하는 서버 목록 리스트                    |
|                                  |             | `totalPages`          | Integer                            | 전체 페이지 수                               |
|                                  |             | `totalElements`       | Long                               | 전체 도메인 서버 개수                           |
|                                  |             | `currentPage`         | Integer                            | 현재 페이지 번호 (0부터 시작)                     |

---

### 6. 🖼️ Mock API 관리 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명**                                                                         |
| --- | --- | --- | --- |--------------------------------------------------------------------------------|
| **`MockApiCreateRequest`** | Mock API 생성 요청 | `name` | String | API 라벨/이름 (선택 사항)                                                              |
|  |  | `httpMethod` | String | HTTP 메서드                                                                       |
|  |  | `endpointPath` | String | API 경로                                                                         |
|  |  | `responseBody` | String | 응답 JSON/TEXT                                                                   |
|  |  | `statusCode` | Integer | HTTP 상태 코드                                                                     |
| **`MockApiUpdateRequest`** | Mock API 수정 요청 | `name` | String | API 라벨/이름 (선택 사항)                                                              |
|  |  | `httpMethod` | String | HTTP 메서드                                                                       |
|  |  | `responseBody` | String | 응답 JSON/TEXT                                                                   |
|  |  | `statusCode` | Integer | HTTP 상태 코드                                                                     |
|  |  | `isActive` | Boolean | 활성화 여부                                                                         |
| **`MockApiCoreResponse`** | **[Base DTO] Mock API 핵심 정보** | `id` | Long | Mock API 고유 ID                                                                 |
|  |  | `name` | String | API 라벨/이름                                                                      |
|  |  | `httpMethod` | String | HTTP 메서드                                                                       |
|  |  | `endpointPath` | String | API 경로                                                                         |
|  |  | `fullEndpointUrl` | String | 호출용 전체 API 경로 (`/mock/{projectId}/{serverSlug}/{endpointPath}` 를 조합하여 생성 후 반환) |
|  |  | `statusCode` | Integer | HTTP 상태 코드                                                                     |
|  |  | `isActive` | Boolean | 활성화 여부                                                                         |
| **`MockApiCreateResponse`** | Mock API 생성 성공 응답 | **`MockApiCoreResponse`** | **(임베드)** | **`MockApiCoreResponse`의 모든 필드**                                               |
|  |  | `responseBody` | String | 응답 JSON/TEXT                                                                   |
|  |  | `createdAt` | Instant | 생성 일시                                                                          |
| **`MockApiUpdateResponse`** | Mock API 수정 성공 응답 | **`MockApiCoreResponse`** | **(임베드)** | **`MockApiCoreResponse`의 모든 필드**                                               |
|  |  | `responseBody` | String | 응답 JSON/TEXT                                                                   |
|  |  | `updatedAt` | Instant | 최종 수정 일시                                                                       |
| **`MockApiDetailResponse`** | Mock API 상세 정보 응답 | **`MockApiCoreResponse`** | **(임베드)** | **`MockApiCoreResponse`의 모든 필드**                                               |
|  |  | `responseBody` | String | 응답 본문                                                                          |
|  |  | `createdAt` | Instant | 생성 일시                                                                          |
|  |  | `updatedAt` | Instant | 최종 수정 일시                                                                       |
| **`MockApiResponse`** | Mock API 정보 목록 응답 (response body 제외) | **`MockApiCoreResponse`** | **(임베드)** | **`MockApiCoreResponse`의 모든 필드**                                               |
|  |  | `updatedAt` | Instant | 최종 수정 일시                                                                       |
| **`MockApiGroupDto`** | Mock API 목록 그룹 dto | `groupName` | String | 그룹핑 이름 (예: User, Product)                                                      |
|  |  | `mocks` | List<`MockApiResponse`> | 해당 그룹에 속한 개별 Mock API의 핵심 정보 리스트                                               |
| **`MockApiListResponse`** | Mock API 목록 응답 | `mocks` | List<`MockApiGroupDto`> | 그룹핑된 Mock API 목록                                                               |
|  |  | `nextCursorId` | Long | 다음 페이지를 요청할 때 사용해야 할 커서 ID                                                     |
|  |  | `hasNext` | Boolean | 다음 페이지(그룹)가 존재하는지 여부                                                           |
| **`MockApiBulkResponse`** | YAML 일괄 처리 응답 | `processedCount` | Integer | 처리된 Mock API 건수                                                                |
|  |  | `errors` | List<String> | 처리 중 발생한 오류 목록                                                                 |

---

### 7. 🔒 Webhook Secret 관리 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명** |
| --- | --- | --- | --- | --- |
| **`WebhookSecretResponse`** | Secret Key 정보 응답 | `projectId` | Long | 연결된 프로젝트 ID |
|  |  | `isActive` | Boolean | 키 활성화 여부 |
|  |  | `secretKey` | String | **새로 발급된 Secret Key 값** (재발급 시에만 포함) |
| **`WebhookTokenIssueRequest`** | JWT 발급 요청 | `secretKey` | String | **사용자 소유의 Secret Key** |
| **`WebhookTokenResponse`** | Webhook JWT 응답 | `jwtToken` | String | **30일 유효 Webhook JWT 토큰** |
|  |  | `expiresIn` | Long | 만료 시간 (초 단위) |
| **`DeploymentEventRequest`** | WebHook 수신 요청 | `projectName` | String | 배포된 프로젝트 이름 (Webhook JWT에서 추출 가능하지만 포함 권장) |
|  |  | `status` | String | 배포 상태 (성공, 실패 등) |
|  |  | `healthCheckUrl` | String | 배포된 서버의 헬스 체크 URL |
|  |  | `healthCheckInterval` | String | 배포된 서버의 헬스 체크 주기 (미선택 시 기본 10분) |

---

### 8. 📧 초대 관련 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입** | **설명** |
| --- | --- | --- | --- | --- |
| **`PagedInvitationListResponse`** | 진행 중인 초대 정보 목록 (페이징) | `invitaions`  | List<`InvitationInfo`> | 현재 페이지의 초대 정보 목록 |
|  |  | `totalElements` | Long | 전체 초대장 수 |
|  |  | `totalPages` | Integer | 전체 페이지 수 |
|  |  | `currentPage` | Integer | 현재 페이지 번호 (0부터 시작) |
| **`InvitationInfo`** | 진행 중인 초대 목록 내부 DTO | `invitationId` | Long | 초대 고유 ID |
|  |  | `invitedEmail` | String | 초대 대상 이메일 |
|  |  | `expiresAt` | Instant | 만료 시간 |
|  |  | `memberRole`  | String | 부여될 역할 |
| **`InvitationCreateRequest`** | 팀원 초대 요청 | `email` | String | 초대 대상의 사용자 이메일 주소 |
|  |  | `memberRole`  | String | 초대 시 부여할 프로젝트 내 역할 (`MANAGER`, `DEVELOPER`, `VIEWER` 중 하나 - 기본 `VIEWER`) |
| **`InvitationCreateResponse`** | 초대 성공 응답 | `invitationId` | Long | 새로 생성된 초대장 고유 ID |
|  |  | `invitedEmail` | String | 초대 대상 이메일 |

---

### 9. Job Tracking 관련 DTO

| **DTO 클래스명** | **역할** | **필드명** | **타입**  | **설명**                                     |
| --- | --- | --- |---------|--------------------------------------------|
| **`JobStatusResponse`** | 비동기 작업 상태 조회 응답 | `jobId`  | Long    | Job의 고유 ID                                 |
|  |  | `status` | String  | 작업 상태 (`PROCESSING`, `SUCCESS`, `FAILURE`) |
|  |  | `submittedAt` | Instant | 작업 요청 시간                                   |
|  |  | `completedAt` | Instant | 작업 완료 시간                                   |
|  |  | `totalParsed` | int | 파일에서 파싱된 총 API 개수                          |
|  |  | `successCount` | int | 성공적으로 삽입된 API 개수                           |
|  |  | `duplicateCount` | int | 중복으로 인해 스킵된 API 개수                         |
|  |  | `message` | String | 사용자에게 보여줄 간단한 성공/실패 메시지                    |
|  |  | `detailedError` | String | 실패 시 상세 에러 로그 (개발자/운영자용)                   |