## 🚀 알림 시스템 기술 요구사항 및 설계 명세

### 1. 📢 알림 엔티티 (`NotificationEntity`) 및 데이터 모델

알림 엔티티는 서비스 전체의 이벤트를 기록하는 단일 테이블로 설계되었으며, `recipientUserId`와 `domainServerId` 필드를 통해 알림의 귀속 대상을 유연하게 지정합니다.

| **구분** | **설계 원칙** | **상세 구현 방안** |
| --- | --- | --- |
| **스코프** | 서비스 전체 알림 처리 | `service_notification` 테이블명을 사용하여 서비스 전반의 알림을 포괄합니다. | 
| **귀속 대상** | 유연한 Nullable PK | `recipientUserId` (Long) 및 `domainServerId` (Long) 필드는 Nullable이며, **둘 중 최소 하나는 반드시 값을 가져야 합니다.** (서비스 레이어에서 강제) | 
| **추가 정보** | `metadata` 활용 | 초대 ID, 실패 응답 코드, 리다이렉트 경로 등 이벤트 처리에 필요한 모든 동적 정보는 `metadata` (TEXT/JSON String)에 저장합니다. |
| **상태 관리** | `isRead` 플래그 | 알림을 읽었는지 여부를 표시하며, 이 플래그를 기반으로 미확인 알림 카운트가 집계됩니다. | 
| **관계** | ID 기반 단방향 참조 | 모든 관계(User, DomainServer)는 직접적인 JPA 매핑 없이 ID 필드만 유지하여 엔티티 간의 의존성을 최소화합니다 | 

### 2. 🔔 알림 정책 및 상태 관리 전략

알림은 **개인 귀속**과 **서버 귀속** 두 가지 정책으로 운영되며, 이 정책에 따라 읽음 처리 방식이 다릅니다.

#### A. 사용자 귀속 알림 (User-Specific Notifications)

| **유형**   | **예시**    | **정책**    | **읽음 처리** |
|--- | --- | --- |-----------|
| **개인 알림**   | 멤버 초대장, 시스템 공지 등    | **개인별 상태 관리.**    | **사용자 1명만** 읽음 처리가 됩니다.          |
| **기록**   |     | 읽음 상태(`isRead=true`)가 된 알림도 삭제되기 전까지 `NotificationPageResponse에` 표시됩니다.   |           |
| **삭제**   |     | `DELETE /api/v1/notifications/{notificationId}` 요청을 통해 **개별 사용자만** 자신의 알림 기록을 삭제할 수 있습니다   |           |

#### B. 서버 귀속 알림 (Shared Server Notifications)

| **유형**   | **예시**    | **정책**    | **읽음 처리** |
|--- | --- | --- |-----------|
| **운영 알림**   | 헬스 체크 실패, 배포 상태 변경 등   | **팀 공유 상태 관리.**    | 프로젝트 멤버 중 **누군가 한 명**이 읽음 처리하면, 해당 서버를 공유하는 **모든 사용자에게** 읽음으로 표시됩니다.          |
| **UX 가이드**   |     | 프론트엔드에서 **"읽음 상태는 프로젝트 멤버 전체에게 공유됩니다."**와 같은 안내 문구를 제공하여 UX 혼란을 방지합니다.   |           |

### 3. 📊 데이터 처리 및 집계 로직

알림 기능의 핵심은 효율적인 카운트 집계와 데이터 변환에 있습니다.

#### A. 미확인 알림 카운트 집계 (unreadNotificationCount)

| **대상 DTO**   | **집계 기준**    | **로직 상세**    |
|--- | --- | --- |
| `ProjectDto`   | 프로젝트 단위   | **해당 프로젝트 내 모든 DomainServer**에 귀속된 `NotificationEntity` 중 `isRead=false`인 알림의 총 개수를 합산하여 제공.    | 
| `DomainServerDto`   | 서버 단위    | **해당 DomainServer**에 귀속된 `NotificationEntity` 중 `isRead=false`인 알림의 **총 개수**를 제공.   |
| `NotificationPageResponse`   | 사용자 단위    | `recipientUserId`가 현재 사용자인 `NotificationEntity` 중 `isRead=false`인 알림의 **총 개수**를 제공.   |

#### B. metadata 활용 및 데이터 변환

| **필드**   | **활용 목적**    | **데이터 변환 요구사항**    |
|--- | --- | --- |
| `NotificationDto.redirectUrl`   | **UX 개선 (클릭 시 이동)**   | 프론트엔드는 알림의 `type` 필드와 `metadata` (JSON Node)에 포함된 `projectId`, `serverId` 등의 정보를 조합하여 동적인 리다이렉트 URL을 생성합니다. (예: `INVITATION` 타입 시 `/project/{projectId}/invite`).    | 
| `HealthCheckFailureLogDto.failureReason`   | 로그 가독성 확보    | 백엔드 또는 DTO 변환 레이어에서 `NotificationEntity`의 `title` 필드를 직접 사용하거나, `metadata`를 파싱하여 실패 응답 코드 등의 구체적인 상세 정보를 추가하여 사용자에게 명확한 실패 이유를 제공합니다.   |