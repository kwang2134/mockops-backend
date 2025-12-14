# entity_mock_api

## 📄 entity_notification.md

### 1. 엔티티 개요 (`Notification`)

| **항목** | **내용**                                                                                                          |
| --- |-----------------------------------------------------------------------------------------------------------------|
| **클래스명** | `Notification`                                                                                                   |
| **테이블명** | `service_notification`                                                                                             |
| **설명** | 서비스 전체에서 발생하는 중요 이벤트(헬스 체크 실패, 멤버 초대, 시스템 공지 등)를 기록합니다. 알림은 특정 `userId` 또는 `domainServerId`에 귀속됩니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함)                                                         |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다.                                                     |
| **유니크 제약** | None (동일 서버 또는 사용자에게 여러 알림이 동시에 발생 가능)                      |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입**          | **DB 타입**   | **설명**                                  | **제약 조건 및 JPA 매핑**                                        |
| --- |--------------------|-------------|-----------------------------------------|-----------------------------------------------------------|
| **`id`** | `Long`             | `BIGINT`    | PK (고유 식별자)                             | `@Id`, `@GeneratedValue(strategy = IDENTITY)`             |
| **`recipientUserId`** | `Long` | `BIGINT`    | **알림을 받을 사용자 ID** (사용자에게 직접 보내는 알림인 경우) | `@Column(name = "user_id"), Nullable`                     |
| **`domainServerId`**  | `Long` | `BIGINT`   | **알림이 귀속된 대상 서버 ID** (프로젝트 관련 알림인 경우)       | `@Column(name = "server_id"), Nullable`                   |
| **`type`** | `NotificationType`(Enum)    | `VARCHAR` | 작업이 요청된 시각알림 유형 (아래 6. 참고) | `@Enumerated(EnumType.STRING), @Column(nullable = false)` |
| **`title`** | `String`           | `VARCHAR` | 알림 제목 (예: "Health Check 실패", "새 멤버 초대") | `@Column(nullable = false, length = 100)`                 |
| **`message`** | `String`           | `TEXT`       | 상세 알림 내용                       | `@Lob`, Nullable                                                  |
| **`isRead`** | `Boolean`          | `BOOLEAN`       | 사용자가 확인했는지 여부 (기본값: false)                   | `@Column(nullable = false)`                               |
| **`metadata`** | `String`          | `TEXT`   | 이벤트에 필요한 추가 데이터 (예: 초대 사용자 ID, 실패 Endpoint URL 등 JSON String)                         | `@Lob`, Nullable                                          |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`recipientUserId`, `domainServerId`, `type`, `title`, `message`, `isRead`, `metadata`**를 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**


| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`User`** | Many-to-One       | **매핑하지 않음**          | `recipientUserId` 필드를 통해 ID만 저장.       |
| **`DomainServer`** | Many-to-One | **매핑하지 않음** | `domainServerId ` 필드를 통해 ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **알림의 역할**: `NotificationEntity`는 오직 **발생한 이벤트의 기록**만을 목적으로 합니다.
> 2. **알림 귀속 원칙**: 유의미한 알림이 되기 위해서는 `recipientUserId` **또는** `domainServerId` **둘 중 최소 하나는 반드시 값을 가져야 합니다.** (이는 서비스 레이어에서 강제해야 할 로직 제약입니다.)
> 3. **관계 회피**: 다른 엔티티와의 직접적인 관계 매핑을 하지 않으며, 연관 엔티티의 ID만 유지하여 데이터 단순성과 시스템 부하를 최소화합니다.

---

### 6. Enum 정의 (`NotificationType`)

알림의 타입을 정의하는 Enum 클래스를 정의합니다.

| **Enum 값**                       | **설명**                                 |
|----------------------------------|----------------------------------------|
| **`HEALTH_CHECK_FAILURE`**       | WebHook 헬스 체크 실패 발생 (DomainServer 귀속)  |
| **`SERVER_STATUS_CHANGED`**      | 도메인 서버 상태 변경 발생 (DomainServer 귀속)      |
| **`MEMBER_INVITATION_RECEIVED`** | 프로젝트 멤버 초대장 수신 (User 귀속)               |
| **`MEMBER_INVITATION_ACCEPTED`** | 초대 수락/거절 결과 (User/DomainServer 귀속)     |
| **`MOCK_BULK_SUCCESS`**          | Mock API 일괄 생성 작업 성공 (DomainServer 귀속) |
| **`MOCK_BULK_FAILURE`**          | Mock API 일괄 생성 작업 실패 (DomainServer 귀속) |
| **`SYSTEM_ANNOUNCEMENT`**        | 서비스 전체 공지 (User 또는 Null 귀속)            |
