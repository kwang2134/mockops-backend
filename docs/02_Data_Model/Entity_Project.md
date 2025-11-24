# entity_project

## 📄 entity_project.md

### 1. 엔티티 개요 (`Project`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `Project` |
| **테이블명** | `projects` |
| **설명** | MockOps의 최상위 관리 단위. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`name`** | `String` | `VARCHAR(100)` | 프로젝트 이름 | `@Column(nullable = false, unique = true)` |
| **`description`** | `String` | `VARCHAR(500)` | 프로젝트 설명 | `@Column(nullable = true)` |
| **`ownerId`** | `Long` | `BIGINT` | **프로젝트 생성자(`User`)의 ID** (FK 역할) | `@Column(nullable = false, name = "owner_id")` |
| **`slackWebhookUrl`** | `String` | `VARCHAR(255)` | 상태 변경 알림을 보낼 슬랙 웹훅 URL | `@Column(nullable = true)` |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`name`, `description`, `ownerId`, `slackWebhookUrl`*을 포함합니다.

---

### 4. 관계 정의 (Relationships)

단방향 연관관계 전략에 따라 **`User`**와의 관계는 `ownerId` 필드로 처리됩니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`User`** (Owner) | Many-to-One | **매핑하지 않음** | `ownerId` 필드를 통해 User ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `Project` 엔티티 내에 **`DomainServer`**, **`ProjectMember`**, **`ProjectCorsOrigin`** 등 **`@OneToMany`** 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 해당 목록 데이터가 필요할 경우, **`Project` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점(예: 하이버네이트의 배치 사이즈 쿼리 최적화)이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.
>
> **즉, `Project` 엔티티 클래스에는 현재 `@OneToMany` 관계 필드를 명시적으로 작성하지 않습니다.**
>