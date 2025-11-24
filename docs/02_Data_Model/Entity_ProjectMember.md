# entity_project_member

## 📄 entity_project_member.md

### 1. 엔티티 개요 (`ProjectMember`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `ProjectMember` |
| **테이블명** | `project_members` |
| **설명** | 프로젝트에 소속된 팀원의 정보와 역할을 정의하는 중간(Many-to-Many 관계 해소) 엔티티입니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |
| **유니크 제약** | `project_id`와 `user_id`의 조합은 **유니크**해야 합니다. (한 사용자가 한 프로젝트에 중복 참여 불가) |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`projectId`** | `Long` | `BIGINT` | **소속 프로젝트(`Project`)의 ID** (FK 역할) | `@Column(nullable = false, name = "project_id")` |
| **`userId`** | `Long` | `BIGINT` | **팀원(`User`)의 ID** (FK 역할) | `@Column(nullable = false, name = "user_id")` |
| **`memberRole`** | `MemberRole` (Enum) | `VARCHAR` | 프로젝트 내 역할 | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`projectId`, `userId`, `memberRole`*을 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

`ProjectMember`는 **`Project` ID**와 **`User` ID**를 단순 `Long` 타입 필드로 관리하며, 직접적인 Many-to-One 연관관계 매핑은 수행하지 않습니다. 이는 조회 시 복잡한 매핑 대신 ID 기반 조회를 활용하기 위함입니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`Project`** | Many-to-One | **매핑하지 않음** | `projectId` 필드를 통해 ID만 저장. |
| **`User`** | Many-to-One | **매핑하지 않음** | `userId` 필드를 통해 ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `ProjectMember` 엔티티 내에 `@OneToMany` 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 하위 목록 데이터가 필요할 경우, **`ProjectMember` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.

---

### 6. Enum 정의 (`MemberRole`)

프로젝트 내 팀원 역할을 나타내는 Enum 클래스를 정의합니다.

| Enum 값 | **권한 범위 (MockOps)** |
| --- | --- |
| **`OWNER`** | **최상위 관리자**. 모든 권한을 가짐. (프로젝트 삭제, 키 재발급, 모든 멤버의 역할 변경 및 제외, 모든 서버 삭제 포함) |
| **`MANAGER`** | **팀 관리자/PM**. `DEVELOPER`와 `VIEWER`의 **초대, 역할 변경, 제외** 가능. **`DomainServer` 삭제** 가능. 모든 Mock API 관리. |
| **`DEVELOPER`** | **실무 개발자**. Mock API 생성/수정/삭제. **`DomainServer` 생성 및 수정** 가능. |
| **`VIEWER`** | 단순 조회 권한. |