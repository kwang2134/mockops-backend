# entity_auth_provider

## 📄 entity_auth_provider.md

### 1. 엔티티 개요 (`AuthProvider`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `AuthProvider` |
| **테이블명** | `auth_providers` |
| **설명** | 사용자의 소셜 로그인(OAuth 2.0) 정보를 저장하는 엔티티입니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | `@Builder` 패턴 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`providerType`** | `ProviderType` (Enum) | `VARCHAR` | 소셜 제공자 종류 (Google, Kakao 등) | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |
| **`providerId`** | `String` | `VARCHAR(255)` | 각 제공자별 사용자 고유 ID | `@Column(nullable = false)` |
| **`refreshToken`** | `String` | `VARCHAR(512)` | **OAuth 리프레시 토큰** (접근 토큰 갱신용) | `@Column(nullable = true)` |
| **`userId`** | `Long` | `BIGINT` | **연결된 `User`의 ID** (FK 역할) | `@Column(nullable = false, name = "user_id")` |

**Note:**

1. **접근 토큰 대신 리프레시 토큰**을 저장합니다. 리프레시 토큰은 만료 기간이 길고 민감도가 높아 반드시 **양방향 암호화**를 사용하여 저장해야 합니다.
2. `providerId`와 `providerType`은 **복합 유니크 제약**이 적용되어야 합니다.

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`providerType`, `providerId`, `refreshToken`, `userId`*를 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

- `User` 엔티티 객체(`User user`) 대신 **`userId` 필드(Long 타입)**를 사용하여 연관관계를 설정합니다.
- 이는 **Many-to-One 연관관계 매핑 없이** 순수하게 `Long` 타입의 ID를 관리하여 JPA의 복잡한 프록시 로딩을 방지하고, **DB 조회 시 필요한 ID만 저장**함으로써 성능을 최적화합니다.

---

### 5. Enum 정의 (`ProviderType`)

소셜 제공자 종류를 나타내는 Enum 클래스를 정의합니다.

| **Enum 값** | **설명** |
| --- | --- |
| **`GOOGLE`** | 구글 |
| **`KAKAO`** | 카카오 |
| **`NAVER`** | 네이버 |
| **`GITHUB`** | 깃허브 |