# entity_project_cors_origin

## 📄 entity_project_cors_origin.md

### 1. 엔티티 개요 (`ProjectCorsOrigin`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `ProjectCorsOrigin` |
| **테이블명** | `project_cors_origins` |
| **설명** | 프로젝트의 Mock API 사용을 위해 프론트엔드에서 접근이 허용된 Origin URL 목록을 저장합니다. **CORS 동적 관리 및 캐싱의 기초 데이터**가 됩니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |
| **유니크 제약** | `project_id`와 `origin_url`의 조합은 **유니크**해야 합니다. (한 프로젝트에 동일한 Origin URL 중복 등록 불가) |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`projectId`** | `Long` | `BIGINT` | **소속 프로젝트(`Project`)의 ID** (FK 역할) | `@Column(nullable = false, name = "project_id")` |
| **`originUrl`** | `String` | `VARCHAR(255)` | CORS 접근이 허용된 URL (예: `http://localhost:3000`) | `@Column(nullable = false)` |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`projectId`, `originUrl`*을 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

- `ProjectCorsOrigin`은 **`Project` ID**를 단순 `Long` 타입 필드로 관리하며, 직접적인 Many-to-One 연관관계 매핑을 수행하지 않습니다. 이는 CORS 검증 로직이 ID를 통해 캐시를 조회하는 방식으로 최적화되기 때문입니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`Project`** | Many-to-One | **매핑하지 않음** | `projectId` 필드를 통해 ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `ProjectCorsOrigin` 엔티티 내에 `@OneToMany` 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 하위 목록 데이터가 필요할 경우, **`ProjectCorsOrigin` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.