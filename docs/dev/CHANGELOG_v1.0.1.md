# Release Notes v1.0.1

**릴리즈 날짜:** 2024-12-24

---

## 1. 백엔드 추가 기능

### 1.1 프로젝트 검색 API

**엔드포인트:** `GET /api/v1/projects/search`

**기능 설명:**
- QueryDSL 기반 동적 검색 기능 추가
- 프로젝트 제목과 오너 nickname으로 프로젝트 검색 가능
- 모든 검색 조건은 선택사항 (null 가능)

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명                                        |
|---------|------|------|------|-------------------------------------------|
| `name` | String | X | Query | 프로젝트 제목 부분 일치 검색 (대소문자 구분 없음)             |
| `nickname` | String | X | Query | 프로젝트 오너 nickname 포함                       |
| `page` | Integer | X | Query | 페이지 번호 (0부터 시작, 기본값: 0)                   |
| `size` | Integer | X | Query | 페이지 크기 (기본값: 20)                          |
| `sort` | String | X | Query | 정렬 기준 (예: `createdAt,desc` 또는 `name,asc`) |

**요청 예시:**
```
# 전체 목록 조회 (검색 조건 없음)
GET /api/v1/projects/search?page=0&size=20

# 제목으로 검색
GET /api/v1/projects/search?name=MockOps&page=0&size=20

# 오너 닉네임으로 검색
GET /api/v1/projects/search?ownerNickname=kim&page=0&size=20

# 제목과 오너 닉네임으로 검색
GET /api/v1/projects/search?name=Mock&ownerNickname=kim&page=0&size=20

# 정렬 기준 추가
GET /api/v1/projects/search?name=Mock&sort=createdAt,desc
```

**권한:**
- 인증된 사용자 (서비스 회원)만 사용 가능

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "MockOps",
        "description": "프로젝트 설명",
        "ownerNickname": "사용자닉네임",
        "createdAt": "2024-12-24T10:00:00",
        "unreadNotificationCount": 3
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "currentPage": 0
  }
}
```

---

### 1.2 도메인 서버 검색 API

**엔드포인트:** `GET /api/v1/projects/{projectId}/servers/search`

**기능 설명:**
- QueryDSL 기반 동적 검색 기능 추가
- 도메인 서버 이름과 상태로 검색 가능
- 특정 프로젝트 내에서만 검색 (projectId 필수)

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `projectId` | Long | O | Path | 프로젝트 ID |
| `name` | String | X | Query | 도메인 서버 이름 부분 일치 검색 (대소문자 구분 없음) |
| `status` | String | X | Query | 서버 상태 (`MOCKING`, `PROXYING`, `DOWN`) |
| `page` | Integer | X | Query | 페이지 번호 (0부터 시작, 기본값: 0) |
| `size` | Integer | X | Query | 페이지 크기 (기본값: 20) |
| `sort` | String | X | Query | 정렬 기준 (기본값: `createdAt,desc`) |

**요청 예시:**
```
# 전체 목록 조회 (검색 조건 없음)
GET /api/v1/projects/1/servers/search?page=0&size=20

# 이름으로 검색
GET /api/v1/projects/1/servers/search?name=API&page=0&size=20

# 상태로 필터링
GET /api/v1/projects/1/servers/search?status=MOCKING&page=0&size=20

# 이름과 상태로 검색
GET /api/v1/projects/1/servers/search?name=API&status=MOCKING&page=0&size=20

# 정렬 기준 추가
GET /api/v1/projects/1/servers/search?status=PROXYING&sort=name,asc
```

**권한:**
- 프로젝트 멤버(VIEWER) 이상만 사용 가능

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "API Server",
        "slug": "api-server",
        "status": "MOCKING",
        "createdAt": "2024-12-24T10:00:00",
        "unreadNotificationCount": 2
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalPages": 1,
    "totalElements": 1
  }
}
```

---

### 1.3 프로젝트 멤버 닉네임 기능

**새로운 엔드포인트:**
1. `GET /api/v1/projects/{projectId}/members/me`
2. `PATCH /api/v1/projects/{projectId}/members/me/nickname`

**기능 설명:**
- 프로젝트 내에서만 사용되는 별도의 닉네임 기능 추가
- 서비스 전체 닉네임과 분리된 프로젝트별 닉네임 관리
- 프로젝트 참여 시 서비스 닉네임을 기본값으로 자동 설정
- 사용자가 자신의 프로젝트 닉네임을 언제든지 변경 가능

#### 1.3.1 내 프로젝트 멤버 정보 조회

**엔드포인트:** `GET /api/v1/projects/{projectId}/members/me`

**기능:**
- 로그인한 사용자의 프로젝트 멤버 정보 조회
- 프로젝트 내 닉네임, 역할, ID 등 포함

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `projectId` | Long | O | Path | 프로젝트 ID |

**요청 예시:**
```
GET /api/v1/projects/1/members/me
```

**권한:**
- 프로젝트 멤버(VIEWER) 이상

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 5,
    "nickname": "홍길동",
    "projectNickname": "프론트엔드 개발자",
    "memberRole": "DEVELOPER"
  }
}
```

**응답 필드:**
- `id`: 프로젝트 멤버 ID
- `userId`: 사용자 ID
- `nickname`: 서비스 전체 닉네임
- `projectNickname`: 프로젝트 내 닉네임 (변경 가능)
- `memberRole`: 프로젝트 내 역할 (OWNER, MANAGER, DEVELOPER, VIEWER)

#### 1.3.2 내 프로젝트 닉네임 수정

**엔드포인트:** `PATCH /api/v1/projects/{projectId}/members/me/nickname`

**기능:**
- 로그인한 사용자의 프로젝트 내 닉네임 수정
- 본인의 닉네임만 수정 가능

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `projectId` | Long | O | Path | 프로젝트 ID |
| `projectNickname` | String | O | Body | 새로운 프로젝트 닉네임 (1-50자) |

**요청 예시:**
```
PATCH /api/v1/projects/1/members/me/nickname
Content-Type: application/json

{
  "projectNickname": "백엔드 팀장"
}
```

**유효성 검증:**
- 필수 입력 (빈 값 불가)
- 1자 이상 50자 이하

**권한:**
- 프로젝트 멤버(VIEWER) 이상 (본인만 가능)

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 5,
    "nickname": "홍길동",
    "projectNickname": "백엔드 팀장",
    "memberRole": "DEVELOPER"
  }
}
```

#### 1.3.3 멤버 목록 조회 변경사항

**엔드포인트:** `GET /api/v1/projects/{projectId}/members`

**변경 내용:**
- 로그인한 사용자를 목록에서 제외하고 반환
- 로그인 사용자는 `/me` API로 별도 조회
- 응답 DTO에 `projectNickname` 필드 추가

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `projectId` | Long | O | Path | 프로젝트 ID |
| `size` | Integer | X | Query | 페이지 크기 (기본값: 20) |
| `offset` | Integer | X | Query | 오프셋 (건너뛸 항목 수, 기본값: 0) |

**요청 예시:**
```
# 첫 페이지 조회
GET /api/v1/projects/1/members?size=20&offset=0

# 다음 페이지 조회 (offset은 응답의 nextOffset 사용)
GET /api/v1/projects/1/members?size=20&offset=20
```

**이유:**
- UI에서 로그인 사용자와 다른 멤버를 분리하여 표시하기 위함
- 로그인 사용자는 상단에 개별 컴포넌트로 표시
- 다른 멤버는 페이징 목록으로 표시

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "members": [
      {
        "id": 11,
        "userId": 6,
        "nickname": "김개발",
        "projectNickname": "프론트엔드 개발자",
        "memberRole": "DEVELOPER"
      }
    ],
    "hasNext": true,
    "nextOffset": 20
  }
}
```

---

### 1.4 도메인 서버 담당 멤버 기능

**새로운 엔드포인트:**
1. `GET /api/v1/servers/{serverId}/members`
2. `POST /api/v1/servers/{serverId}/members/me`
3. `DELETE /api/v1/servers/{serverId}/members/me`

**기능 설명:**
- 프로젝트 멤버가 특정 도메인 서버를 담당하여 개발할 수 있는 기능
- 한 멤버는 하나의 도메인 서버만 담당 가능
- 도메인 서버별로 담당 멤버 목록 조회 및 참여/나가기 기능 제공
- 로그인한 사용자의 참여 정보를 함께 제공하여 UI 상태 관리 용이

#### 1.4.1 도메인 서버 담당 멤버 목록 조회

**엔드포인트:** `GET /api/v1/servers/{serverId}/members`

**기능:**
- 특정 도메인 서버를 담당하는 멤버 목록 조회
- Offset 기반 페이지네이션 사용
- 권한 순서로 정렬 (OWNER → MANAGER → DEVELOPER → VIEWER)
- 로그인 사용자의 도메인 서버 참여 정보 포함

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `serverId` | Long | O | Path | 도메인 서버 ID |
| `size` | Integer | X | Query | 페이지 크기 (기본값: 20) |
| `offset` | Integer | X | Query | 오프셋 (건너뛸 항목 수, 기본값: 0) |

**요청 예시:**
```
# 첫 페이지 조회
GET /api/v1/servers/1/members?size=20&offset=0

# 다음 페이지 조회
GET /api/v1/servers/1/members?size=20&offset=20
```

**권한:**
- 프로젝트 멤버(VIEWER) 이상

**응답 형식:**
```json
{
  "success": true,
  "data": {
    "members": [
      {
        "id": 10,
        "userId": 5,
        "nickname": "홍길동",
        "projectNickname": "백엔드 팀장",
        "memberRole": "DEVELOPER"
      },
      {
        "id": 11,
        "userId": 6,
        "nickname": "김개발",
        "projectNickname": "API 개발자",
        "memberRole": "DEVELOPER"
      }
    ],
    "hasNext": true,
    "nextOffset": 20,
    "myDomainServerId": 2,
    "isParticipating": false
  }
}
```

**응답 필드:**
- `members`: 담당 멤버 목록 (권한 순 정렬)
- `hasNext`: 다음 페이지 존재 여부
- `nextOffset`: 다음 페이지 조회 시 사용할 오프셋 (없으면 null)
- `myDomainServerId`: 로그인 사용자가 현재 담당 중인 도메인 서버 ID (담당 서버 없으면 null)
- `isParticipating`: 로그인 사용자가 현재 조회 중인 서버를 담당하고 있는지 여부

#### 1.4.2 도메인 서버 참여

**엔드포인트:** `POST /api/v1/servers/{serverId}/members/me`

**기능:**
- 로그인한 사용자가 도메인 서버의 담당 멤버로 참여
- 한 멤버는 하나의 도메인 서버만 담당 가능
- 이미 다른 서버를 담당 중이면 참여 불가

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `serverId` | Long | O | Path | 참여할 도메인 서버 ID |

**요청 예시:**
```
POST /api/v1/servers/3/members/me
```

**권한:**
- DEVELOPER 이상

**응답:**
- **204 No Content** (성공)

**에러 응답:**
```json
{
  "success": false,
  "error": {
    "code": "PERMISSION_DENIED",
    "message": "이미 다른 도메인 서버에 참여 중입니다."
  }
}
```

#### 1.4.3 도메인 서버 나가기

**엔드포인트:** `DELETE /api/v1/servers/{serverId}/members/me`

**기능:**
- 로그인한 사용자가 담당 중인 도메인 서버에서 나가기
- 담당하지 않는 서버에서는 나갈 수 없음

**요청 파라미터:**

| 파라미터 | 타입 | 필수 | 위치 | 설명 |
|---------|------|------|------|------|
| `serverId` | Long | O | Path | 나갈 도메인 서버 ID |

**요청 예시:**
```
DELETE /api/v1/servers/3/members/me
```

**권한:**
- DEVELOPER 이상

**응답:**
- **204 No Content** (성공)

**에러 응답:**
```json
{
  "success": false,
  "error": {
    "code": "PERMISSION_DENIED",
    "message": "해당 도메인 서버에 참여하고 있지 않습니다."
  }
}
```
### 1.5 배포 Webhook 요청 기능 개선

**개요:**
기존에는 외부 CI/CD 파이프라인에서 서버 상태를 직접 지정하여 요청했으나, 신뢰성 향상을 위해 **서버가 직접 헬스 체크를 수행하고 결과에 따라 상태를 변경**하도록 개선되었습니다. 또한 대량의 웹훅 요청을 안정적으로 처리하기 위해 **Redis 기반의 비동기 큐 처리 방식**을 도입했습니다.

**주요 변경 사항:**

1. **요청 파라미터 변경:**
   - `healthCheckUrl` 필드가 **필수**로 변경되었습니다.
   - `serverStatus` 필드가 **제거**되었습니다. (서버가 직접 헬스 체크 후 판단)
   - 헬스 체크 주기는 선택사항입니다.

2. **비동기 처리 구조 도입:**
   - 웹훅 요청 시 즉시 Redis에 작업만 등록하고 `202 Accepted`를 반환하여 응답 속도를 보장합니다.
   - **2초 주기 스케줄러**가 Redis에서 작업을 가져와 비동기 이벤트를 발행합니다.
   - 실제 헬스 체크 및 처리는 별도 스레드(비동기 리스너)에서 수행되어 메인 스케줄러의 지연을 방지합니다.

3. **안정적인 상태 관리:**
   - **성공 시:** 헬스 체크 성공 시 서버 상태를 `DEPLOYED`로 변경하고, 서비스 내 정식 헬스 체크 대상으로 등록합니다.
   - **재시도:** 일시적인 네트워크 오류 및 서버 실행 시간을 고려하여 최대 **5회**까지 재시도를 수행합니다.
   - **실패 시:** 5회 연속 실패 시 서버 상태를 `ERROR`로 변경하고, 관리자에게 슬랙 및 서비스 알림을 발송합니다.

**변경된 요청 형식:**

**엔드포인트:** `POST /api/webhook/deploy/{projectId}`

**변경 전 (Deprecated):**
```json
{
  "projectName": "MyProject",
  "domainServerName": "ApiServer",
  "serverStatus": "DEPLOY",  // 삭제됨
  "healthCheckUrl": "https://api.example.com/health", // 선택사항이었음
  "healthCheckInterval": "10m"
}
```

**변경 후:**
```json
{
  "projectName": "MyProject",
  "domainServerName": "ApiServer",
  "healthCheckUrl": "https://api.example.com/health", // 필수!
  "healthCheckInterval": "10m"
}
```

**처리 프로세스:**
1. 웹훅 요청 수신 -> Redis에 `DeployHealthCheckJob` 저장 (Set 구조로 중복 방지)
2. 스케줄러(2초 주기) -> Redis에서 Job 조회 -> `DeploymentHealthcheckEvent` 발행
3. 이벤트 리스너 -> 비동기 스레드에서 헬스 체크 수행
4. 결과 처리:
   - **성공:** 상태 변경(DEPLOYED), 알림 발송, 정식 헬스 체크 등록
   - **실패:** 재시도 횟수 증가 후 Redis 재등록 (최대 5회)
   - **최종 실패:** 상태 변경(ERROR), 실패 알림 발송

---

### 1.6 기술 스택

**QueryDSL 도입:**
- Custom Repository + Impl 패턴 적용
- DDD 아키텍처 준수
  - Custom 인터페이스: domain layer
  - Impl 구현체: infrastructure layer
- 동적 쿼리 생성 (`BooleanBuilder` 사용)
- 검색 조건별 부분 일치/정확히 일치 처리

**페이징 최적화:**
- 생성일 기준 내림차순 정렬 (최신순)
- `Page<T>` 반환으로 총 개수, 페이지 수 정보 포함

---

## 2. 프론트엔드 구현 가이드

### 2.1 기존 목록 조회 API 대체

**중요:** 기존 목록 조회 API 대신 **검색 API를 기본 목록 조회용으로 사용**하세요.

#### 프로젝트 목록
```typescript
// ❌ 기존 방식 (deprecated 예정)
GET /api/v1/projects?page=0&size=10

// ✅ 새로운 방식 (검색 API를 기본 목록으로 사용)
GET /api/v1/projects/search?page=0&size=10
// 검색 파라미터를 생략하면 전체 목록 반환
```

#### 도메인 서버 목록
```typescript
// ❌ 기존 방식 (deprecated 예정)
GET /api/v1/projects/{projectId}/servers?page=0&size=20

// ✅ 새로운 방식 (검색 API를 기본 목록으로 사용)
GET /api/v1/projects/{projectId}/servers/search?page=0&size=20
// 검색 파라미터를 생략하면 전체 목록 반환
```

---

### 2.2 검색 기능 구현 방법

#### 기존 목록 조회 로직 활용
기존에 사용하던 목록 조회 DTO와 로직을 그대로 사용하면 됩니다. 단, API 엔드포인트만 검색 API로 변경하고 검색 파라미터를 추가하세요.

#### 프로젝트 검색
- 기존 프로젝트 목록 조회와 동일한 응답 형식 (`ProjectPageResponse`)을 반환합니다
- Query Parameter로 `name` (프로젝트 제목), `ownerNickname` (오너 닉네임)를 추가할 수 있습니다
- 검색 조건이 없으면 전체 목록을 반환하므로, 기본 목록 조회용으로도 사용 가능합니다
- 검색 입력창과 검색 버튼을 추가하여 사용자가 검색어를 입력할 수 있게 구성하세요

#### 도메인 서버 검색
- 기존 도메인 서버 목록 조회와 동일한 응답 형식 (`Page<DomainServerSimpleResponse>`)을 반환합니다
- Query Parameter로 `name` (서버 이름), `status` (서버 상태)를 추가할 수 있습니다
- 검색 조건이 없으면 전체 목록을 반환하므로, 기본 목록 조회용으로도 사용 가능합니다
- 검색 입력창과 상태 필터 드롭다운을 추가하여 사용자가 검색하고 필터링할 수 있게 구성하세요

---

### 2.3 구현 시 주의사항

1. **빈 검색어 처리**
   - 빈 문자열("")을 검색 파라미터로 보내지 마세요
   - 빈 문자열은 `undefined` 또는 `null`로 변환하여 파라미터에서 제외해야 합니다

2. **페이지 초기화**
   - 검색 실행 시 페이지 번호를 `0`으로 초기화하세요
   - 검색 결과가 이전과 다를 수 있으므로 첫 페이지부터 시작해야 합니다

3. **권한 에러 처리**
   - 401 Unauthorized: 인증 토큰 만료 → 로그인 페이지로 이동
   - 403 Forbidden: 권한 없음 → 접근 거부 메시지 표시
   - 404 Not Found: 프로젝트 없음 → 목록 페이지로 이동

---

### 2.4 마이그레이션 체크리스트

- [ ] 프로젝트 목록 API 엔드포인트 변경: `/api/v1/projects` → `/api/v1/projects/search`
- [ ] 도메인 서버 목록 API 엔드포인트 변경: `/api/v1/projects/{id}/servers` → `/api/v1/projects/{id}/servers/search`
- [ ] 검색 UI 추가 (검색 입력창, 필터 드롭다운, 검색 버튼)
- [ ] 검색 파라미터 state 추가 (검색어, 필터 값)
- [ ] 검색 실행 시 페이지 초기화 로직 추가
- [ ] 빈 검색어 처리 로직 추가 (빈 문자열 → undefined 변환)

---

### 2.5 프로젝트 멤버 닉네임 기능 구현 방법

#### 2.5.1 UI 구조 변경

**기존 구조:**
```
[팀원 목록 컴포넌트]
  - 모든 팀원 표시 (로그인 사용자 포함)
  - 페이징 처리
```

**새로운 구조:**
```
[내 멤버 정보 컴포넌트] ← 새로 추가!
  - 로그인 사용자의 프로젝트 멤버 정보 단독 표시
  - 프로젝트 닉네임, 역할 표시
  - 닉네임 수정 버튼/입력창

[팀원 목록 컴포넌트]
  - 다른 팀원만 표시 (로그인 사용자 제외)
  - 페이징 처리
```

**중요:** 로그인 사용자는 반드시 상단의 별도 컴포넌트로 분리하여 표시해야 합니다. 목록에 포함시키지 마세요.

#### 2.5.2 API 호출 방법

**페이지 진입 시:**

1. **내 멤버 정보 조회** (상단 컴포넌트용)
   - API: `GET /api/v1/projects/{projectId}/members/me`
   - 로그인 사용자의 프로젝트 닉네임, 역할 정보를 가져옵니다
   - 상단의 개별 컴포넌트에 표시합니다

2. **팀원 목록 조회** (목록 컴포넌트용)
   - API: `GET /api/v1/projects/{projectId}/members?size=20&offset=0`
   - 로그인 사용자를 제외한 다른 팀원 목록을 가져옵니다
   - 페이징 목록 컴포넌트에 표시합니다

**닉네임 수정 시:**

1. 사용자가 닉네임 수정 버튼 클릭
2. 입력창 표시 (또는 모달 팝업)
3. 사용자가 새 닉네임 입력 후 저장
4. API 호출: `PATCH /api/v1/projects/{projectId}/members/me/nickname`
   - Body: `{ "projectNickname": "새 닉네임" }`
5. 성공 시 상단 컴포넌트의 닉네임 업데이트

#### 2.5.3 응답 데이터 구조

**내 멤버 정보 응답:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 5,
    "nickname": "홍길동",          // 서비스 전체 닉네임
    "projectNickname": "팀장",     // 프로젝트 내 닉네임
    "memberRole": "MANAGER"
  }
}
```

**팀원 목록 응답:**
```json
{
  "success": true,
  "data": {
    "members": [
      {
        "id": 11,
        "userId": 6,
        "nickname": "김개발",
        "projectNickname": "프론트엔드 개발자",
        "memberRole": "DEVELOPER"
      },
      {
        "id": 12,
        "userId": 7,
        "nickname": "박디자인",
        "projectNickname": "UI/UX 디자이너",
        "memberRole": "VIEWER"
      }
    ],
    "hasNext": true,
    "nextOffset": 20
  }
}
```

#### 2.5.4 구현 시 주의사항

1. **컴포넌트 분리**
   - 내 멤버 정보와 팀원 목록은 반드시 별도 컴포넌트로 분리하세요
   - 내 멤버 정보는 페이징 처리 없이 단독 표시합니다
   - 팀원 목록만 페이징 처리합니다

2. **닉네임 표시**
   - 프로젝트 관련 페이지에서는 `projectNickname`을 우선 표시하세요
   - `nickname`은 참고용 또는 괄호 안에 표시할 수 있습니다
   - 예: "팀장 (홍길동)" 또는 "팀장"만 표시

3. **닉네임 수정 제약**
   - 빈 값 입력 시 에러 메시지 표시 ("프로젝트 닉네임은 필수입니다")
   - 50자 초과 시 에러 메시지 표시 ("1자 이상 50자 이하여야 합니다")
   - 입력 중 실시간 글자 수 표시 권장 (예: "15/50")

4. **에러 처리**
   - 400 Bad Request: 유효성 검증 실패 → 에러 메시지 표시
   - 401 Unauthorized: 인증 토큰 만료 → 로그인 페이지로 이동
   - 403 Forbidden: 권한 없음 → 프로젝트 멤버가 아님, 접근 거부 메시지
   - 404 Not Found: 프로젝트 없음 → 프로젝트 목록으로 이동

5. **UX 개선 제안**
   - 닉네임 수정 후 성공 토스트 메시지 표시
   - 수정 중 로딩 상태 표시 (버튼 비활성화 등)
   - 취소 버튼 제공으로 실수 방지

#### 2.5.5 마이그레이션 체크리스트

- [ ] 내 멤버 정보 컴포넌트 신규 생성 (상단 배치)
- [ ] 팀원 목록 컴포넌트에서 로그인 사용자 제거 로직 삭제 (백엔드에서 제외됨)
- [ ] `/me` API 호출 추가 (페이지 로드 시)
- [ ] 멤버 목록 API는 기존대로 호출 (자동으로 로그인 사용자 제외됨)
- [ ] 닉네임 수정 UI 추가 (입력창, 저장/취소 버튼)
- [ ] 닉네임 수정 API 연동 (`PATCH /members/me/nickname`)
- [ ] 응답 데이터 타입에 `projectNickname` 필드 추가
- [ ] 프로젝트 관련 UI에서 `projectNickname` 우선 표시하도록 변경
- [ ] 유효성 검증 로직 추가 (필수, 1-50자)
- [ ] 에러 처리 로직 추가

---

### 2.6 도메인 서버 담당 멤버 기능 구현 방법

#### 2.6.1 UI 구조

**도메인 서버 상세 페이지 탭 구조:**
```
[도메인 서버 상세 페이지]
  ├─ Mock API 탭
  ├─ 헬스 체크 탭
  └─ 담당 멤버 탭 ← 새로 추가!
      ├─ 멤버 목록 (페이징)
      └─ 참여/나가기 버튼 (상태별 표시)
```

**담당 멤버 탭 내용:**
- 해당 도메인 서버를 담당하는 멤버 목록 표시
- 권한 순서로 정렬 (OWNER → MANAGER → DEVELOPER → VIEWER)
- 페이징 목록 (Offset 기반)
- 로그인 사용자의 참여 상태에 따른 버튼 표시

**중요:** 프로젝트 멤버 목록과 달리 컴포넌트 분리가 필요 없습니다. 전체 목록을 하나의 컴포넌트로 표시하고, 로그인 사용자도 목록에 포함됩니다.

#### 2.6.2 API 호출 방법

**페이지 진입 시:**

1. **담당 멤버 목록 조회**
   - API: `GET /api/v1/servers/{serverId}/members?size=20&offset=0`
   - 해당 도메인 서버를 담당하는 멤버 목록을 가져옵니다
   - 응답에 로그인 사용자의 참여 정보가 포함되어 있습니다

**참여 버튼 클릭 시:**

1. 사용자가 "참여하기" 버튼 클릭
2. API 호출: `POST /api/v1/servers/{serverId}/members/me`
3. 성공 시 (204):
   - 멤버 목록 새로고침 (GET 요청 재호출)
   - 성공 토스트 메시지 표시
4. 실패 시 (400):
   - 에러 메시지 표시 ("이미 다른 도메인 서버에 참여 중입니다")

**나가기 버튼 클릭 시:**

1. 사용자가 "나가기" 버튼 클릭
2. 확인 다이얼로그 표시 (선택사항)
3. API 호출: `DELETE /api/v1/servers/{serverId}/members/me`
4. 성공 시 (204):
   - 멤버 목록 새로고침
   - 성공 토스트 메시지 표시

#### 2.6.3 응답 데이터 구조

**담당 멤버 목록 응답:**
```json
{
  "success": true,
  "data": {
    "members": [
      {
        "id": 10,
        "userId": 5,
        "nickname": "홍길동",
        "projectNickname": "백엔드 팀장",
        "memberRole": "DEVELOPER"
      }
    ],
    "hasNext": true,
    "nextOffset": 20,
    "myDomainServerId": 2,
    "isParticipating": false
  }
}
```

**주요 필드 설명:**
- `myDomainServerId`: 로그인 사용자가 담당 중인 서버 ID
  - `null`: 담당 서버 없음
  - 숫자: 해당 서버 ID를 담당 중
- `isParticipating`: 현재 조회 중인 서버를 담당하고 있는지
  - `true`: 현재 서버 담당 중
  - `false`: 다른 서버 담당 중 또는 담당 서버 없음

#### 2.6.4 UI 상태별 버튼 표시 로직

로그인 사용자의 상태에 따라 다른 UI를 표시해야 합니다:

**1. 담당 서버 없음 (`myDomainServerId === null`)**
```
조건: myDomainServerId === null
표시: [참여하기] 버튼
동작: POST /servers/{serverId}/members/me
권한: DEVELOPER 이상만 버튼 표시
```

**2. 현재 서버 담당 중 (`isParticipating === true`)**
```
조건: isParticipating === true
표시: [나가기] 버튼
동작: DELETE /servers/{serverId}/members/me
스타일: 위험 색상 (빨강)
```

**3. 다른 서버 담당 중 (`myDomainServerId !== null && isParticipating === false`)**
```
조건: myDomainServerId !== null && isParticipating === false
표시: 안내 메시지
내용: "이미 다른 도메인 서버에 참여 중입니다"
추가: 현재 담당 서버로 이동 링크 (선택사항)
버튼: 표시하지 않음
```

**4. VIEWER 권한 사용자**
```
조건: memberRole === "VIEWER"
표시: 조회만 가능 (버튼 없음)
안내: "DEVELOPER 이상만 도메인 서버에 참여할 수 있습니다" (선택사항)
```

#### 2.6.5 구현 시 주의사항

1. **권한 체크**
   - DEVELOPER 이상만 참여/나가기 버튼 표시
   - VIEWER는 목록 조회만 가능

2. **상태 관리**
   - `myDomainServerId`와 `isParticipating`을 state로 관리
   - API 호출 성공 후 목록을 다시 불러와서 최신 상태 반영

3. **에러 처리**
   - 400 Bad Request: "이미 다른 도메인 서버에 참여 중입니다" → 안내 메시지 표시
   - 400 Bad Request: "해당 도메인 서버에 참여하고 있지 않습니다" → 목록 새로고침
   - 401 Unauthorized: 로그인 페이지로 이동
   - 403 Forbidden: "DEVELOPER 이상만 참여할 수 있습니다" 메시지 표시
   - 404 Not Found: "도메인 서버를 찾을 수 없습니다" 메시지 표시

4. **페이징 처리**
   - Offset 기반 페이징 사용
   - `hasNext`가 `true`면 "더보기" 버튼 또는 무한 스크롤 구현
   - 다음 페이지 로드 시 `nextOffset` 값 사용

5. **UX 개선 제안**
   - 참여/나가기 시 로딩 상태 표시 (버튼 비활성화)
   - 성공 시 토스트 메시지 표시
   - 나가기는 확인 다이얼로그 표시 권장 ("정말 나가시겠습니까?")
   - 현재 담당 중인 멤버는 배지나 강조 표시 추가 (선택사항)

6. **데이터 표시**
   - 멤버 목록에는 `projectNickname` 우선 표시
   - 권한(memberRole)을 배지로 표시 (OWNER, MANAGER, DEVELOPER 등)
   - 권한 순으로 정렬되어 있으므로 별도 정렬 불필요

#### 2.6.6 마이그레이션 체크리스트

- [ ] 도메인 서버 상세 페이지에 "담당 멤버" 탭 추가
- [ ] 담당 멤버 목록 컴포넌트 생성 (페이징 포함)
- [ ] `/servers/{serverId}/members` API 호출 추가
- [ ] `myDomainServerId`, `isParticipating` state 관리
- [ ] 상태별 버튼 표시 로직 구현 (참여/나가기/안내)
- [ ] 참여 API 연동 (`POST /servers/{serverId}/members/me`)
- [ ] 나가기 API 연동 (`DELETE /servers/{serverId}/members/me`)
- [ ] DEVELOPER 권한 체크 추가
- [ ] 에러 처리 로직 추가
- [ ] 성공 시 목록 새로고침 로직 추가
- [ ] 로딩 상태 처리
- [ ] 응답 데이터 타입 정의 (TypeScript 사용 시)

---

## 3. Swagger 문서

모든 API는 Swagger UI에서 확인 가능합니다:

**검색 API:**
- **프로젝트 검색:** `/swagger-ui.html` → 프로젝트 → `GET /api/v1/projects/search`
- **도메인 서버 검색:** `/swagger-ui.html` → 도메인 서버 → `GET /api/v1/projects/{projectId}/servers/search`

**프로젝트 멤버 닉네임 API:**
- **내 멤버 정보 조회:** `/swagger-ui.html` → 프로젝트 멤버 → `GET /api/v1/projects/{projectId}/members/me`
- **내 닉네임 수정:** `/swagger-ui.html` → 프로젝트 멤버 → `PATCH /api/v1/projects/{projectId}/members/me/nickname`
- **팀원 목록 조회:** `/swagger-ui.html` → 프로젝트 멤버 → `GET /api/v1/projects/{projectId}/members` (로그인 사용자 제외)

**도메인 서버 담당 멤버 API:**
- **담당 멤버 목록 조회:** `/swagger-ui.html` → 도메인 서버 → `GET /api/v1/servers/{serverId}/members`
- **도메인 서버 참여:** `/swagger-ui.html` → 도메인 서버 → `POST /api/v1/servers/{serverId}/members/me`
- **도메인 서버 나가기:** `/swagger-ui.html` → 도메인 서버 → `DELETE /api/v1/servers/{serverId}/members/me`
