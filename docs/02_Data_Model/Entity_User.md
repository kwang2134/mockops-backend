# enitity_user

## 📄 entity_user.md

### 1. 엔티티 개요 (`User`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `User` |
| **테이블명** | `users` |
| **설명** | MockOps 서비스의 핵심 회원 정보 엔티티. JWT 인증 및 접근 권한 관리에 사용됨. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`email`** | `String` | `VARCHAR(255)` | email(써드파티), 유저 식별자 | `@Column(nullable = false, unique = true)` |
| **`nickname`** | `String` | `VARCHAR(50)` | 사용자 닉네임 | `@Column(nullable = false)` |
| **`role`** | `Role` (Enum) | `VARCHAR` | 사용자 권한 (`USER`, `ADMIN` 등) | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)`를 사용하여 JPA 프록시 생성을 허용합니다.
- **빌더 패턴:** `@Builder`를 사용하여 객체를 생성합니다. 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`email`, `password`, `nickname`, `role`*만 포함합니다.

---

### 4. 관계 정의 (Relationships)

단방향 연관관계 전략 및 **`@OneToMany` 필드 지연 매핑 전략**에 따라, **`User` 엔티티 클래스에는 `List<T>` 타입의 필드를 명시적으로 선언하지 않습니다.**

| **대상 엔티티** | **관계** | **매핑 방식 (참조 목적)** | **설명** |
| --- | --- | --- | --- |
| **`AuthProvider`** | One-to-Many | **필드 미포함** | 필요 시 `AuthProviderRepository`를 통해 `userId`로 조회하여 사용합니다. |
| **`Project`** | One-to-Many | **필드 미포함** | 필요 시 `ProjectRepository`를 통해 `ownerId`로 조회하여 사용합니다. |
| **`ProjectMember`** | One-to-Many | **필드 미포함** | 필요 시 `ProjectMemberRepository`를 통해 `userId`로 조회하여 사용합니다. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `User` 엔티티 내에 **`AuthProvider`**, **`Project`**, **`ProjectMember`** 등 **`@OneToMany`** 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 해당 목록 데이터가 필요할 경우, **`User` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점(예: 하이버네이트의 배치 사이즈 쿼리 최적화)이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.

---

### 6. Enum 정의 (`Role`)

| **Enum 값** | **설명** |
| --- | --- |
| **`USER`** | 일반 사용자 (기본 권한) |
| **`ADMIN`** | 관리자 권한을 가진 사용자 |