# MockOps

개발자: [김광현](https://github.com/kwang2134)

<img width="500" height="500" alt="logo-remove" src="https://github.com/user-attachments/assets/c8a571a4-b81a-49a8-818f-472a7b00b430" />

[MockOps!](https://mockops.cloud)

## 프로젝트 소개

**MockOps**는 프로젝트 단위로 팀원들이 협업하며 **동적 Mock API를 제공**하는 **개발 도구 서비스**입니다. <br>
개발 초기 단계에서 백엔드 API가 준비되지 않아 프론트엔드와 모바일 개발이 지연되는 문제를 해결하고, MSA 환경에서 여러 도메인 서버의 Mock API를 **하나의 대시보드**에서 관리하여 **개발 생산성**을 높이는 것을 목표로 합니다.

Mock API 스펙을 통해 각 서버의 API 설계를 한눈에 파악할 수 있으며, 개발 과정에서 도움이 되도록 **간단한 헬스 체크**와 **서버 상태 표시** 기능도 제공합니다.

<img width="1920" height="1440" alt="main1" src="https://github.com/user-attachments/assets/9b5bce41-a2ef-40bb-86a9-1d92620bfce5" />
<img width="1920" height="1440" alt="main2" src="https://github.com/user-attachments/assets/c3eec1c4-eb9e-427a-beec-f5d072f098ca" />
<img width="1920" height="1440" alt="main3" src="https://github.com/user-attachments/assets/1bd505be-7f26-4386-8b0f-a6cd575832d5" />

<br>

## 사용 기술

### 개발

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Security**
- **Spring Security OAuth2 Client**
- **JPA/Hibernate**
- **Spring Data Redis**
- **JWT (jjwt)**
- **WebFlux (헬스 체크용)**
- **SpringDoc OpenAPI (Swagger)**

### DB

- **MySQL**: Mock API 메타데이터, 프로젝트 정보, 사용자 인증 정보를 저장하기 위한 RDBMS
- **Redis**:
  - **헬스 체크 작업 관리**: 주기별(5m, 10m, 30m, 1h) 헬스 체크 대상 서버 목록 저장
  - **Mock API 캐싱**: 동적 라우팅 성능 최적화를 위한 Mock API 응답 캐싱
  - **CORS Origin 캐싱**: 프로젝트별 허용 Origin 목록 캐싱으로 검증 성능 향상

### 인프라

- **AWS EC2**: Docker 컨테이너 기반 애플리케이션 배포 환경
- **Docker & Docker Compose**: 컨테이너화를 통한 일관된 배포 환경 구성

### CI/CD

- **GitHub Actions**: 코드 빌드, 테스트, Docker 이미지 빌드 및 배포 파이프라인 자동화
- **Docker**: 컨테이너화를 통한 일관된 배포 환경 구성
- **Prometheus & Grafana**: 애플리케이션 모니터링 및 메트릭 수집

## ERD
<!-- ERD 이미지 위치 -->
<img width="1560" height="1052" alt="mockops" src="https://github.com/user-attachments/assets/a93a7f65-551f-4516-b81c-d0b5f884be4a" />


## 시스템 아키텍처
<!-- 시스템 아키텍처 이미지 위치 -->
<img width="1545" height="1779" alt="sys_arcr_bg" src="https://github.com/user-attachments/assets/437359f5-7513-4bfa-811a-6a34215ba294" />


## 핵심 기능

### Mock API 동적 제공 시스템

- **동적 라우팅**: `/mock/{projectId}/{serverName}/**` 패턴으로 실시간 Mock API 응답 제공
- **HTTP 메서드 지원**: GET, POST, PUT, DELETE 등 모든 HTTP 메서드 지원
- **패턴 매칭**: Spring AntPathMatcher를 활용한 와일드카드 경로 매칭 (`/users/*`, `/api/**` 등)
- **응답 커스터마이징**: HTTP 상태 코드, 응답 Body 설정 가능
- **OpenAPI 스펙 지원**: YAML/JSON 파일 업로드를 통한 Mock API 일괄 등록

### 프로젝트 및 팀 협업 관리

- **프로젝트 생성 및 관리**: 프로젝트 단위로 Mock API와 도메인 서버를 그룹화하여 관리
- **팀원 초대 및 권한 관리**: 이메일 기반 초대 시스템, 역할별 권한 관리 (OWNER, MANAGER, DEVELOPER, VIEWER)
- **역할 기반 접근 제어**:
  - OWNER: 프로젝트 삭제, 멤버 관리, 모든 설정 변경 권한
  - MANAGER: DEVELOPER 권한 + 멤버 초대 권한
  - DEVELOPER: Mock API 및 서버 관리
  - VIEWER: 읽기 전용 권한

### 도메인 서버 상태 대시보드

- **서버 상태 관리**: MOCKING → DEPLOYED → ERROR 상태 추적
- **실시간 상태 확인**: 각 도메인 서버의 현재 상태를 대시보드에서 한눈에 확인
- **WebHook 기반 자동 상태 갱신**: CI/CD 파이프라인에서 배포 완료 시 WebHook을 통해 자동으로 상태 변경
- **헬스 체크 에러 기록**: 에러로 인한 헬스 체크 실패 로그 기록

### 주기적 헬스 체크 시스템

- **다양한 체크 주기**: 5분, 10분, 30분, 1시간 단위 선택 가능
- **Redis 기반 작업 관리**: 주기별로 헬스 체크 대상을 Redis Set에 저장하여 효율적인 스케줄링
- **자동 상태 전환**: 3회 연속 실패 시 서버 상태를 ERROR로 자동 변경
- **Slack 알림 연동**: 헬스 체크로 인한 서버 상태 변경 시 프로젝트별로 설정된 Slack Webhook으로 즉시 알림 전송
- **WebClient 기반 비동기 처리**: Spring WebFlux를 활용한 비동기 헬스 체크로 성능 최적화

### WebHook 기반 배포 상태 관리

- **CI/CD 통합**: GitHub Actions, Jenkins 등 배포 파이프라인과 연동
- **프로젝트별 인증**: WebhookSecret 테이블에 저장된 JWT 시크릿으로 요청 검증
- **자동 헬스 체크 등록**: 배포 시 전송한 웹훅에 입력한 헬스 체크 URL과 주기를 자동으로 Redis에 등록
- **조건부 헬스 체크 활성화**: healthCheckUrl이 제공된 경우에만 헬스 체크 활성화

### 인증/인가 시스템

- **소셜 로그인**: OAuth 2.0 기반 Google, Kakao, Naver, GitHub 로그인 지원
- **JWT 인증**: Access Token/Refresh Token 기반 stateless 인증
- **이중 인증 체계**:
  - MockOps 관리 API용 JWT (사용자 세션 관리)
  - WebHook API용 프로젝트별 JWT (배포 파이프라인 인증)
- **사용자 약관 동의 관리**: 이용약관 및 개인정보처리방침 버전별 동의 이력 관리, JWT Claim 기반 동의 검증

### 알림 시스템

- **Slack Webhook 연동**: 헬스 체크 실패, 서버 상태 변경 등 주요 이벤트 알림
- **이메일 알림**: 팀원 초대, 프로젝트 권한 변경 등 사용자 액션 알림

### 성능 최적화

- **Redis 캐싱 전략**:
  - Mock API 응답 캐싱 (24시간 TTL)
  - CORS Origin 목록 캐싱 (24시간 TTL)
  - Cache-Aside 패턴 적용
- **Batch 헬스 체크**: 여러 서버의 헬스 체크를 벌크로 처리하여 효율성 향상

## 아키텍처 특징

### Clean Architecture 기반 패키지 구조

- **도메인 기반 모듈 설계**: 각 도메인(user, project, mock, healthcheck)별로 독립적인 패키지 구성
- **계층 분리**: Presentation, Domain, Infrastructure 계층의 명확한 관심사 분리
- **의존성 역전**: Port/Adapter 패턴을 통한 외부 기술(Redis, 메일) 의존성 캡슐화

### Redis 기반 분산 작업 관리

- **헬스 체크 작업 분산**: Redis Set을 활용하여 주기별로 헬스 체크 대상 서버 분류
- **작업 이동**: 서버의 헬스 체크 주기 변경 시 Redis 간 작업 원자적 이동

### WebHook 인증 체계

- **프로젝트별 시크릿 관리**: 각 프로젝트마다 독립적인 JWT 시크릿 키 발급 및 암호화 저장
- **양방향 암호화**: AES-256-GCM을 사용하여 시크릿 키 암호화/복호화
- **즉시 폐기 기능**: isActive 플래그를 통한 시크릿 키 즉시 무효화

### 스케줄링 시스템

- **Spring Scheduler**: 주기별 헬스 체크 작업을 @Scheduled로 자동 실행
- **Retry 로직**: @Retry 어노테이션을 활용한 일시적 장애 자동 복구
- **실패 추적**: 연속 실패 횟수를 Redis에 기록하여 장애 판단 기준으로 활용

### 시간 관리 체계

- **UTC 기반 통합**: 모든 시간 데이터를 Instant(UTC) 타입으로 통일하여 타임존 문제 방지
- **클라이언트 변환**: 서버는 UTC로 저장/응답, 클라이언트에서 로컬 시간대로 변환
- **Hibernate 설정**: time_zone=UTC 설정으로 DB 레벨 시간 통일

## 프로젝트 구조

```
src/main/java/com/mockops/
├── domain/                      # 도메인별 비즈니스 로직
│   ├── auth/                   # 인증/인가 시스템
│   ├── user/                   # 사용자 관리
│   ├── agreement/              # 사용자 약관 동의
│   ├── project/                # 프로젝트 관리
│   ├── mock/                   # Mock API 및 도메인 서버
│   ├── healthcheck/            # 헬스 체크 시스템
│   ├── webhook/                # WebHook 처리
│   ├── invitation/             # 초대 시스템
│   ├── job/                    # 백그라운드 작업 추적
│   └── notification/           # 알림 시스템
├── global/                     # 공통 설정 및 유틸리티
│   ├── config/                 # 설정 클래스 (Redis, Security, JWT 등)
│   ├── security/               # 보안 설정 (필터, 핸들러)
│   ├── exception/              # 예외 처리
│   └── handler/                # 공통 핸들러 (Mock 동적 라우팅)
├── infrastructure/             # 외부 기술 연동
│   └── cache/                  # Redis 캐시 구현체
└── presentation/               # API 컨트롤러
    └── api/                    # REST API
```

각 도메인은 다음 구조를 따름

```
domain/{domain-name}/
├── entity/          # JPA 엔티티
├── enums/           # 도메인 열거형
├── repository/      # 데이터 접근 계층
├── service/         # 비즈니스 로직
├── event/           # 도메인 이벤트
└── infrastructure/  # Port 인터페이스 (외부 의존성 추상화)
```

## 배포 환경

**프로덕션 배포**

- AWS EC2 기반 Docker 컨테이너 배포
- GitHub Actions를 통한 CI/CD 파이프라인
- Prometheus + Grafana 모니터링

**환경별 프로필**

- `local`: 로컬 개발 환경 
- `prod`: 운영 환경 

## 개발 문서

### 📋 01. 기획 및 개요
- [프로젝트 개요](docs/01_Plan_And_Overview/01_Project_Overview.md)

### 📊 02. 데이터 모델
- [AuthProvider 엔티티](docs/02_Data_Model/Entity_AuthProvider.md)
- [BaseEntity](docs/02_Data_Model/Entity_BaseEntity.md)
- [DomainServer 엔티티](docs/02_Data_Model/Entity_DomainServer.md)
- [Invitation 엔티티](docs/02_Data_Model/Entity_Invitation.md)
- [JobTracking 엔티티](docs/02_Data_Model/Entity_JobTracking.md)
- [MockApi 엔티티](docs/02_Data_Model/Entity_MockApi.md)
- [Notification 엔티티](docs/02_Data_Model/Entity_Notification.md)
- [Project 엔티티](docs/02_Data_Model/Entity_Project.md)
- [ProjectCorsOrigin 엔티티](docs/02_Data_Model/Entity_ProjectCorsOrigin.md)
- [ProjectMember 엔티티](docs/02_Data_Model/Entity_ProjectMember.md)
- [User 엔티티](docs/02_Data_Model/Entity_User.md)
- [WebhookSecret 엔티티](docs/02_Data_Model/Entity_WebhookSecret.md)

### 🔌 03. 통신 스펙
- [API 명세서 v1](docs/03_Communication_Specs/01_API_Specifications_v1.md)
- [DTO 명세서 v1](docs/03_Communication_Specs/02_DTO_Specifications_v1.md)
- [프론트엔드 연동 가이드](docs/03_Communication_Specs/03_Frontend_Integration.md)

### 🛠️ 04. 기술 및 인프라
- [패키지 구조](docs/04_Tech_And_Infra/01_Package_Structure.md)
- [기술 스택 및 라이브러리](docs/04_Tech_And_Infra/02_Tech_Stack_And_Libraries.md)
- [핵심 기술 스펙: 인증 및 보안](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Auth_Security.md)
- [핵심 기술 스펙: Mock API 일괄 생성](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Bulk_Mock_Creation.md)
- [핵심 기술 스펙: 메일 시스템](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Mail.md)
- [핵심 기술 스펙: Mock API 캐싱](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Mocking_Caching.md)
- [핵심 기술 스펙: 알림 시스템](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Notification.md)
- [핵심 기술 스펙: Redis 복구 전략](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Redis_Recovery_Strategy.md)
- [핵심 기술 스펙: WebHook](docs/04_Tech_And_Infra/03_Core_Tech_Specs_Webhook.md)
- [예외 처리 정책](docs/04_Tech_And_Infra/04_Exception_Handling_Policy.md)

## 릴리즈 노트
[릴리즈 노트](RELEASE_NOTES.md)

### 변경사항 기록
[mockops_v1.0.1](docs/dev/CHANGELOG_v1.0.1.md)