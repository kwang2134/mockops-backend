# entity_invitation

# 📄 entity_invitation.md

## 1. 엔티티 개요 (`Invitation`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `Invitation` |
| **테이블명** | `invitations` |
| **설명** | 프로젝트 팀원 초대를 기록하고 관리하는 엔티티입니다. 발급된 JWT 토큰의 유효성 검증 및 상태 추적에 사용됩니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 `BaseEntity` 필드는 빌더에서 제외됩니다. |
| **유니크 제약** | 없음. (동일 프로젝트에 동일 인물 재초대 가능) |

---

## 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`projectId`** | `Long` | `BIGINT` | **소속 프로젝트(`Project`)의 ID** (FK 역할) | `@Column(nullable = false, name = "project_id")` |
| **`inviterId`** | `Long` | `BIGINT` | **초대한 사용자(`User`)의 ID** (FK 역할) | `@Column(nullable = false, name = "inviter_id")` |
| **`invitedEmail`** | `String` | `VARCHAR(255)` | 초대 대상의 이메일 (로그인 ID) | `@Column(nullable = false)` |
| **`tokenValue`** | `String` | `VARCHAR(512)` | 발급된 JWT 토큰 자체 또는 **토큰의 해시값** (DB 저장용) | `@Column(nullable = false, unique = true)` |
| **`expiresAt`** | `Instant` | `TIMESTAMP` | 초대가 만료되는 시점 | `@Column(nullable = false)` |
| **`status`** | `InvitationStatus` (Enum) | `VARCHAR` | 초대 상태 | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |

---

## 3. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

`Invitation`은 `projectId`와 `inviterId` 필드를 `Long` 타입으로 관리하며, 복잡한 연관관계 매핑은 수행하지 않고 ID 기반 조회를 활용합니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`Project`** | Many-to-One | **매핑하지 않음** | `projectId` 필드를 통해 ID만 저장. |
| **`User` (Inviter)** | Many-to-One | **매핑하지 않음** | `inviterId` 필드를 통해 ID만 저장. |

---

## 4. Enum 정의 (`InvitationStatus`)

초대 상태를 나타내는 Enum 클래스를 정의합니다.

| **Enum 값** | **설명** |
| --- | --- |
| **`PENDING`** | 초대장이 발송되었으나 아직 수락되지 않은 상태. (기본값) |
| **`ACCEPTED`** | 초대 링크를 통해 프로젝트 참여가 성공적으로 수락된 상태. |
| **`EXPIRED`** | 정해진 만료 시간(`expiresAt`)을 지나 더 이상 유효하지 않은 상태. |
| **`CANCELED`** | 초대한 사용자(`Inviter`)가 수락 전에 초대를 취소한 상태. |