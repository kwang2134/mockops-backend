# entity_mock_api

## 📄 entity_mock_api.md

### 1. 엔티티 개요 (`MockApi`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `MockApi` |
| **테이블명** | `mock_apis` |
| **설명** | 특정 도메인 서버의 엔드포인트에 대한 가짜(Mock) 응답 정보를 저장합니다. Mocking 요청 처리의 기반 데이터입니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |
| **유니크 제약** | `serverId`, `httpMethod`, `endpointPath`의 조합은 **유니크**해야 합니다. (한 서버에 동일 경로+메서드 중복 Mock 불가) |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`serverId`** | `Long` | `BIGINT` | **소속 도메인 서버(`DomainServer`)의 ID** (FK 역할) | `@Column(nullable = false, name = "server_id")` |
| **`name`**  | `String`  | `VARCHAR`  | API 라벨/이름 (선택적) | `@Column(nullable = false)` |
| **`httpMethod`** | `HttpMethod` (Enum) | `VARCHAR` | HTTP 메서드 (GET, POST, PUT, DELETE 등) | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |
| **`endpointPath`** | `String` | `VARCHAR(255)` | Mock API 경로 (예: `/users/{id}`) | `@Column(nullable = false)` |
| **`responseBody`** | `String` | `TEXT` | 반환할 Mock 응답 데이터 (JSON 또는 XML 형태) | `@Lob`, `@Column(nullable = false)` |
| **`statusCode`** | `Integer` | `INT` | 반환할 HTTP 상태 코드 (기본값: 200) | `@Column(nullable = false)` |
| **`isActive`** | `Boolean` | `BOOLEAN` | Mock API 활성화 여부 (토글 기능) | `@Column(nullable = false)` |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`serverId`, `httpMethod`, `endpointPath`, `responseBody`, `statusCode`, `isActive`*를 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

`MockApi`는 **`DomainServer` ID**를 단순 `Long` 타입 필드로 관리합니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`DomainServer`** | Many-to-One | **매핑하지 않음** | `serverId` 필드를 통해 ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `MockApi` 엔티티 내에 `@OneToMany` 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 하위 목록 데이터가 필요할 경우, **`MockApi` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.

---

### 6. Enum 정의 (`HttpMethod`)

REST API에서 사용되는 주요 HTTP 메서드를 나타내는 Enum 클래스를 정의합니다.

| **Enum 값** | **설명** |
| --- | --- |
| **`GET`** | 리소스 조회 |
| **`POST`** | 리소스 생성 |
| **`PUT`** | 리소스 전체 수정 |
| **`PATCH`** | 리소스 일부 수정 |
| **`DELETE`** | 리소스 삭제 |