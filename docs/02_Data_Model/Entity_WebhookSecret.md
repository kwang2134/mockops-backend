# entity_webhook_secret

## 📄 entity_webhook_secret.md

### 1. 엔티티 개요 (`WebhookSecret`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `WebhookSecret` |
| **테이블명** | `webhook_secrets` |
| **설명** | 프로젝트별 CI/CD WebHook 요청의 **JWT 서명 및 검증**에 사용되는 고유 비밀 키를 저장합니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |
| **유니크 제약** | `project_id`는 **유니크**해야 합니다. (하나의 프로젝트는 하나의 인증 키만 가짐) |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **제약 조건 및 JPA 매핑** |
| --- | --- | --- | --- | --- |
| **`id`** | `Long` | `BIGINT` | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)` |
| **`projectId`** | `Long` | `BIGINT` | **연결된 프로젝트(`Project`)의 ID** (FK 역할) | `@Column(nullable = false, unique = true, name = "project_id")` |
| **`secretKey`** | `String` | `VARCHAR(255)` | **JWT 서명/검증에 사용되는 비밀 키. UUID 형태의 난수로 생성.** | `@Column(nullable = false)` |
| **`isActive`** | `Boolean` | `BOOLEAN` | 키 활성화 여부. `false` 시 모든 JWT 토큰 즉시 폐기. | `@Column(nullable = false)` |

### 3. 📢 WebHook JWT 토큰 정책 (추가 내용)

> Secret Key 생성: secretKey는 UUID 형태의 난수로 생성되어 사용자에게 한 번 노출됩니다. 이 키 자체는 만료 기간이 없습니다.JWT 유효 기간: 이 secretKey를 통해 발급되는 WebHook JWT 토큰은 30일의 유효 기간을 가집니다. CI/CD 환경에서는 만료 시 토큰을 갱신해야 합니다.즉시 폐기: isActive가 false로 설정되면, 유효 기간이 남은 JWT 토큰이라도 MockOps 서버에서 즉시 인증이 거부됩니다.
>

---

### 4. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt` 필드를 제외한 **`projectId`, `secretKey`, `isActive`*를 포함합니다.

---

### 5. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`Project`** | One-to-One | **매핑하지 않음** | `projectId` 필드를 통해 ID만 저장. |

### 6. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `WebhookSecret` 엔티티 내에 `@OneToMany` 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 하위 목록 데이터가 필요할 경우, **`WebhookSecret` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.

---