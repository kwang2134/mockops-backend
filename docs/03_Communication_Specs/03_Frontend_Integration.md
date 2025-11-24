# fe_integration_spec

## 🔑 🖥️ 1. 인증 및 토큰 관리 플로우

### 1.1 OAuth2 로그인 플로우 (Frontend → Backend 주도)

사용자가 **Google** 또는 **GitHub** 로그인 버튼을 클릭하면, 프론트엔드는 백엔드의 OAuth 시작 엔드포인트로 리다이렉트 요청을 보냅니다.

| **단계** | **주체** | **요청/동작** | **설명** |
| --- | --- | --- | --- |
| **1. 로그인 시작** | Frontend | `GET /api/v1/auth/{provider}/login` | `provider` 자리에 `google` 또는 `github`를 넣어 백엔드에 요청합니다. |
| **2. OAuth Provider 이동** | Backend | 302 Redirect | 백엔드가 사용자 브라우저를 **OAuth Provider의 인증 페이지**로 리다이렉트합니다. |
| **3. 인증 및 Code 발급** | OAuth Provider | 302 Redirect | 사용자가 로그인에 성공하면, OAuth Provider가 **인가 코드(Code)**를 백엔드로 전달합니다. |
| **4. 토큰 발급 및 리다이렉트** | Backend | 302 Redirect | 백엔드가 인가 코드를 이용해 토큰을 발급한 후, 최종적으로 프론트엔드의 **정해진 리다이렉트 URL**로 토큰을 실어 보냅니다. |
| **5. 토큰 수신 및 저장** | Frontend | `Front-end Success URL` | 프론트엔드는 URL 쿼리 파라미터나 헤더에서 **Access Token**을 수신하고 저장합니다. **Refresh Token**은 **HttpOnly Cookie**로 자동 저장됩니다. |

### 1.2 🛡️ 토큰 저장 위치 및 사용 정책

토큰의 종류에 따라 보안 위험도와 사용 방식이 다르므로, 저장 위치를 명확히 분리합니다.

| **토큰** | **저장 위치 (Frontend)** | **사용 용도** | **보안 정책** |
| --- | --- | --- | --- |
| **Access Token** | **메모리** 또는 **Session Storage** | 모든 REST API 요청 시 **`Authorization: Bearer <Token>`** 헤더에 포함하여 사용합니다. | XSS 위험을 최소화하기 위해 **Local Storage 사용을 금지**하고, 만료 시간(30분)을 짧게 유지합니다. |
| **Refresh Token** | **HttpOnly Cookie** (자동) | Access Token 만료 시 **자동 갱신**에만 사용됩니다. | 프론트엔드 **JavaScript 접근이 불가능**하여 XSS 공격으로부터 안전합니다. **`Secure` 속성**으로 HTTPS 통신에서만 전송됩니다. |

### 1.3 🔄 Access Token 갱신 (Silent Refresh) 플로우

프론트엔드는 API 요청 시 **401 Unauthorized** 응답을 받았을 때 자동으로 토큰 갱신을 시도하는 **HTTP 인터셉터**를 구현해야 합니다.

| **단계** | **주체** | **요청/응답** | **설명** |
| --- | --- | --- | --- |
| **1. 만료 감지** | Frontend | API 요청 → **401 Unauthorized** | Access Token 만료를 감지합니다. |
| **2. 갱신 요청** | Frontend | `GET /api/v1/auth/token/refresh` | **자동**으로 갱신 엔드포인트에 요청합니다. 이 요청은 **HttpOnly Cookie**에 담긴 **Refresh Token**을 자동으로 포함합니다. |
| **3. 토큰 재발급** | Backend | **200 OK** (`TokenResponse`) | 백엔드는 DB에 저장된 Refresh Token을 확인 후, 새로운 **Access Token**을 Body에, 새로운 **Refresh Token**을 `HttpOnly Cookie`에 담아 응답합니다. |
| **4. 토큰 업데이트 및 재시도** | Frontend | **API 요청 재시도** | 프론트엔드는 새로운 Access Token을 저장하고, **원래 실패했던 API 요청**을 즉시 다시 시도합니다. |
| **5. Refresh Token 만료** | Frontend | **401 Unauthorized** | 갱신 요청 시에도 401 응답이 오면 **Refresh Token도 만료**된 것이므로, 사용자에게 **재로그인을 유도**합니다. |

### 1.4 로그아웃 플로우

로그아웃은 서버의 Refresh Token을 무효화하여 모든 세션을 종료하는 작업입니다.

- **로그아웃 요청:** `POST /api/v1/auth/logout`
- **프론트엔드 처리:** 요청 성공 후 **Session Storage의 Access Token을 제거**하고, 브라우저의 **쿠키에서 Refresh Token도 제거**하도록 백엔드에서 쿠키 만료 응답을 함께 보냅니다.

---

## 2. 🗺️ API 엔드포인트 그룹별 요약

프론트엔드 개발자가 API 명세서를 효율적으로 사용할 수 있도록 기능 영역별로 핵심 엔드포인트를 요약합니다.

### 2.1. 사용자 및 인증 (User & Auth)

| **기능** | **HTTP Method** | **URL** | **인증/인가** | **주요 DTO** |
| --- | --- | --- | --- | --- |
| **로그인 시작** | `GET` | `/api/v1/auth/{provider}/login` | None | Redirect |
| **토큰 갱신** | `GET` | `/api/v1/auth/token/refresh` | None (Refresh Token Cookie 필요) | `TokenResponse` |
| **로그아웃** | `POST` | `/api/v1/auth/logout` | **`MEMBER`** | `204 No Content` |
| **본인 정보 조회** | `GET` | `/api/v1/user/me` | **`MEMBER`** | `UserResponse` |

### 2.2. 프로젝트 및 멤버 관리 (Project & Member)

| **기능** | **HTTP Method** | **URL** | **인증/인가** | **페이징 방식** | **주요 DTO** |
| --- | --- | --- | --- | --- | --- |
| **프로젝트 목록 조회** | `GET` | `/api/v1/projects` | `MEMBER` | **페이지 기반** | `ProjectPageResponse` |
| **프로젝트 멤버 목록 조회** | `GET` | `/api/v1/projects/{projectId}/members` | `PROJECT_MEMBER` | **커서 기반** | `MemberListResponse` |

### 2.3. 도메인 서버 및 Mock API 관리 (Server & Mock)

| **기능** | **HTTP Method** | **URL** | **인증/인가** | **페이징 방식** | **주요 DTO** |
| --- | --- | --- | --- | --- | --- |
| **서버 생성** | `POST` | `/api/v1/projects/{projectId}/servers` | `PROJECT_MANAGER` | N/A | `DomainServerResponse` |
| **서버 목록 조회** | `GET` | `/api/v1/projects/{projectId}/servers` | `PROJECT_MEMBER` | **페이지 기반** | **`DomainServerPageResponse`** |
| **Mock API 목록 조회** | `GET` | `/api/v1/projects/{projectId}/servers/{serverId}/mocks` | `PROJECT_MEMBER` | **커서 기반** | **`MockApiGroupedResponse`** |
| **Mock API 호출** | ALL | `/mock/{projectId}/{serverName}/**` | None | N/A | `MockApiResponse` |

---

## 3. ⚙️ 페이징 및 커서 처리 가이드라인

MockOps는 목록의 특성에 따라 두 가지 페이징 방식을 혼용합니다.

### 3.1. 페이지 기반 페이징 (Offset 기반)

**적용 대상:** **`프로젝트 목록`**, **`도메인 서버 목록`** 등 페이지 번호 클릭 UI에 적합한 경우.

- **요청 파라미터:** `page` (페이지 번호, 0부터 시작), `size` (페이지당 개수, 기본 10개)
- **핵심 응답 필드:** `totalPages`, `totalElements`, `currentPage`
- **`DomainServerPageResponse` 목록 DTO:** `serverId`, `serverName`, `status` 등 최소 정보만 포함됩니다.

### 3.2. 커서 기반 페이징 (무한 스크롤용)

**적용 대상:** **`프로젝트 멤버 목록`**, **`Mock API 목록`** 등 무한 스크롤 및 성능 최적화가 필요한 경우.

- **요청 파라미터:** `cursorId` (이전 페이지 마지막 ID), `size`
- **핵심 응답 필드:** `nextCursorId`, `hasNext`

---

## 4. 🎯 Mock API 그룹핑 규약

`MockApiGroupedResponse`를 처리할 때 프론트엔드는 다음 규약을 따릅니다.

1. **그룹핑 소스:** 그룹 이름(`groupName`)은 백엔드에서 **Mock API의 URL 경로 첫 번째 세그먼트**를 자동으로 파생하여 생성합니다. (예: `/users/profile` → Group Name: **users**)
2. **구조:** 응답은 **`groups`** 리스트를 포함하며, 프론트엔드는 이 리스트를 기준으로 **탭** 또는 **아코디언 메뉴**를 구성하여 사용자가 API를 기능별로 탐색하도록 구현합니다.
3. **페이징 범위:** 커서 기반 페이징은 **전체 그룹 리스트**에 적용됩니다.

---

## 5. 🔗 통합 응답 및 에러 처리 정책 (Unified Response Policy)

MockOps의 모든 API 응답은 **웹 표준 HTTP 상태 코드를 유지**하며, 응답 본문에 **`success` 필드**를 포함하는 통합된 래퍼 구조를 따릅니다. 프론트엔드는 이 구조를 통해 성공/실패 여부를 일관성 있게 처리합니다.

### 5.1. 통합 응답 래퍼 구조 (`UnifiedResponse<T>`)

| **필드명** | **타입** | **설명** |
| --- | --- | --- |
| **`success`** | `Boolean` | 요청의 최종 성공 여부. **`true`** (성공) 또는 **`false`** (실패) |
| **`result`** | `T` 또는 `ErrorResponse` | 성공 시 실제 데이터(`T`), 실패 시 에러 상세 정보(`ErrorResponse`) |
| **`timestamp`** | `String` | 서버 응답 시각 |

### 5.2. 성공 응답 처리 (`success: true`)

성공적인 API 요청은 적절한 HTTP 상태 코드를 사용하며, `success` 필드는 `true`입니다.

| **항목** | **값** |
| --- | --- |
| **HTTP Status Code** | 200 OK, 201 Created, 204 No Content 등 웹 표준 코드 |
| **`success` 필드** | `true` |
| **`result` 필드** | 실제 응답 DTO (`UserResponse`, `ProjectPageResponse` 등) |

### 5.3. 실패 응답 처리 (`success: false`)

실패 요청은 **HTTP 에러 상태 코드**를 사용하며, `success` 필드는 `false`입니다. 프론트엔드 인터셉터는 이 응답을 받아 `ErrorResponse`를 분석합니다.

### A. 최종 확정된 `ErrorResponse` 구조

백엔드 **Global Exception Handler**에서 처리되어 응답의 `result` 필드에 포함됩니다.

| **필드명** | **타입** | **설명** |
| --- | --- | --- |
| **`success`** | `Boolean` | **`false`** 고정 |
| **`code`** | **`String`** | 백엔드 정의 비즈니스 오류 코드 (예: `USER_NOT_FOUND`) |
| **`message`** | `String` | 개발자 디버깅용 메시지 |
| **`status`** | `Integer` | 응답 HTTP 상태 코드와 동일 (예: 404, 401) |
| **`path`** | `String` | 오류가 발생한 요청 경로 (디버깅 목적) |

### B. 프론트엔드 에러 처리 정책

프론트엔드는 응답의 `success` 필드가 `false`일 때, `result` 필드를 기반으로 다음 핵심 로직을 실행합니다.

| **result.status** | **result.code 유형** | **프론트엔드 동작 (Action)** |
| --- | --- | --- |
| **401 Unauthorized** | `AUTH_TOKEN_EXPIRED` | **토큰 갱신 플로우 실행.** 갱신 실패 시 재로그인 유도. |
| **403 Forbidden** | `PROJECT_FORBIDDEN` | 사용자에게 **권한 부족 알림** 표시. |
| **400 Bad Request** | `INVALID_INPUT` | **입력 폼 필드** 아래에 인라인 오류 메시지 표시. |
| **404 Not Found** | `RESOURCE_NOT_FOUND` | 친절한 **오류 페이지**로 리다이렉트. |
| **5xx Server Error** | `SERVER_ERROR` | 사용자에게 **서비스 장애 알림** 표시 및 에러 로깅. |