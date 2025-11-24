# 패키지 구조

## 📁 project_structure.md

### 1. 🚀 최상위 패키지 구조

MockOps 프로젝트의 최상위 패키지는 `com.mockops.project`가 될 것이며, 주요 구성 요소는 다음과 같이 네 가지로 분리됩니다.

| **패키지** | **역할** | **상세 설명** |
| --- | --- | --- |
| **`domain`** | **핵심 도메인 로직** | 비즈니스 핵심 로직, 엔티티, 서비스, 리포지토리 인터페이스 정의. |
| **`api`** (or `presentation`) | **표현 계층** | 외부 요청을 받고 응답하는 Controller와 **외부 통신용 DTO** 정의. |
| **`global`** | **전역 및 공통 설정** | 예외 처리, 유틸리티, 시큐리티 설정, 공통 인터페이스, Web Config 등. |
| **`infrastructure`** | **인프라/외부 연동** | DB 구현체, 스케줄러 구현체, 외부 서비스(Slack, OAuth) 연동 구현체 등. |

---

### 2. 🧱 `domain` 패키지 상세 구조

`domain`은 비즈니스 규칙이 모여있는 핵심 영역입니다. 각 엔티티(`User`, `Project` 등)별로 하위 패키지를 구성하여 모듈화합니다.

```java
// 루트 패키지: com.mockops

com.mockops.domain
├── user              // 1. 사용자 및 인증 컨텍스트
│   ├── entity
│   │   ├── User.java
│   │   └── AuthProvider.java
│   ├── repository
│   │   ├── UserRepository.java
│   │   └── AuthProviderRepository.java
│   ├── service
│   │   └── UserService.java (로그인, 회원가입, 사용자 정보 관리)
│   └── role            // 도메인 고유 Enum
│       └── Role.java   // (User 역할: ADMIN, MEMBER 등)
|
├── project           // 2. 프로젝트 관리 컨텍스트
│   ├── entity
│   │   ├── Project.java
│   │   ├── ProjectMember.java
│   │   ├── Invitation.java
│   │   └── ProjectCorsOrigin.java
│   ├── repository
│   │   ├── ProjectRepository.java
│   │   └── ProjectMemberRepository.java
│   ├── service
│   │   └── ProjectService.java (생성, 수정, 초대, 멤버 관리 로직)
│   └── role            // 도메인 고유 Enum
│       └── MemberRole.java // (ProjectMember 역할: OWNER, MANAGER 등)
|
└── mock              // 3. Mock 서비스 및 라우팅 컨텍스트
├── entity
│   ├── DomainServer.java
│   ├── MockApi.java
│   └── WebhookSecret.java
├── repository
│   ├── MockApiRepository.java
│   └── DomainServerRepository.java
├── service
│   └── MockService.java (Mock 데이터 처리, AntPathMatcher 로직)
└── handler         // Custom HandlerMapping 관련 로직
└── MockopsHandlerMapping.java
```
---

### 3. 🌐 `api` (Presentation) 패키지 상세 구조

`api` 패키지는 클라이언트와 직접 통신하며, HTTP 요청을 처리하고 응답을 반환하는 역할을 합니다. DTO는 이 계층에서 정의하여 도메인 모델과의 의존성을 분리합니다.

```java
com.mockops.api
├── [entity_name] (예: user, project)
│   ├── UserController.java, ProjectController.java (Controller)
│   └── dto (외부 통신용 DTO 모음)
│       ├── UserResponse.java (응답 DTO)
│       ├── UserRequest.java (요청 DTO)
│       └── ProjectCreateRequest.java
└── mockops
    └── MockingController.java (Mock API 요청 처리 전용 컨트롤러)
```

**[DTO 위치 정책]**

- **Request/Response DTO**는 **`api` 계층** 내에 정의하여 도메인 모델의 필드 변경이 외부 API에 미치는 영향을 최소화합니다.
- **도메인 내부**에서만 사용되는 DTO나 Mapper는 필요하다면 **`domain`** 하위에 정의할 수 있습니다.

---

### 4. 🛠️ `global` 패키지 상세 구조

`global`은 애플리케이션 전반에 걸쳐 사용되는 공통 기능과 설정을 관리합니다.

```java
com.mockops.global
├── exception (커스텀 예외 클래스 및 예외 처리 핸들러)
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── security (JWT 인증, Spring Security 설정)
│   ├── JwtProvider.java (JWT 생성/검증)
│   └── SecurityConfig.java
├── config (WebConfig, JPA Auditing 설정 등)
├── util (공통 유틸리티 클래스: CryptUtils, DateUtils 등)
└── common (공통 인터페이스나 Base 클래스: BaseResponse 등)
```

---

### 5. ⚙️ `infrastructure` 패키지 상세 구조

`infrastructure`는 `domain`에서 정의된 인터페이스의 **구현체**와 외부 시스템과의 연동을 담당합니다.

```java
com.mockops.infrastructure
├── persistence (DB 연동 구현체)
│   ├── UserJpaRepository.java (Domain의 UserRepository 구현)
│   └── project
│       ├── ProjectJpaRepository.java
│       └── entity (선택 사항: JPA 전용 Entity 또는 Converter)
├── external (외부 서비스 연동)
│   ├── SlackNotifier.java (Slack Webhook 구현)
│   └── oauth (OAuth2 연동 관련 구현)
└── scheduler (스케줄러 구현)
    └── HealthCheckScheduler.java (주기적 헬스 체크 로직 구현)
```