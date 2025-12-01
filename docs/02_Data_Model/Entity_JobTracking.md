# entity_mock_api

## 📄 entity_job_tracking.md

### 1. 엔티티 개요 (`JobEntity`)

| **항목** | **내용**                                                                                                          |
| --- |-----------------------------------------------------------------------------------------------------------------|
| **클래스명** | `JobEntity`                                                                                                   |
| **테이블명** | `mock_api_bulk_job`                                                                                             |
| **설명** | OpenAPI 파일 업로드 후 실행되는 비동기 벌크 작업의 상태와 최종 결과를 저장합니다. 클라이언트의 상태 조회(Polling) 기반 데이터입니다.                                           |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함)                                                         |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다.                                                     |
| **유니크 제약** | None (각 작업은 고유하며, 중복 제약은 없음)                      |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입**          | **DB 타입**   | **설명**                                        | **제약 조건 및 JPA 매핑**                                        |
| --- |--------------------|-------------|-----------------------------------------------|-----------------------------------------------------------|
| **`id`** | `Long`             | `BIGINT`    | PK (고유 식별자)                                   | `@Id`, `@GeneratedValue(strategy = IDENTITY)`             |
| **`serverId`** | `Long`             | `BIGINT`    | **대상 Mock API 서버 ID**  | `@Column(nullable = false, name = "server_id")`           |
| **`status`**  | `JobStatus` (Enum) | `VARCHAR`   | 작업 상태 (PROCESSING, SUCCESS, FAILURE)                              | `@Enumerated(EnumType.STRING), @Column(nullable = false)` |
| **`submittedAt`** | `Instant`    | `TIMESTAMP` | 작업이 요청된 시각           | `@Column(nullable = false)`                               |
| **`completedAt`** | `Instant`           | `TIMESTAMP` | 작업이 완료된 시각          | `Nullable`                                                |
| **`totalCount`** | `Integer`           | `INT`       | 파일에서 파싱된 총 API 개수            | `@Column(nullable = false)`                               |
| **`insertedCount`** | `Integer`          | `INT`       | 성공적으로 DB에 삽입된 API 개수                     | `@Column(nullable = false)`                               |
| **`errorMessage`** | `String`          | `TEXT`   | 실패 시 상세 오류 메시지                       | `@Lob`, Nullable                                                  |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt`, `completedAt`, `errorMessage` 필드를 제외한 **`serverId`, `status`, `submittedAt`, `totalCount`, `insertedCount`**를 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**


| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`DomainServer`** | Many-to-One | **매핑하지 않음** | `serverId` 필드를 통해 ID만 저장. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **Job Tracking의 역할**: `JobEntity`는 Mock API 자체를 포함할 필요가 없습니다. 오직 **상태 추적**만을 목적으로 합니다.
> 2. **관계 회피**: 다른 엔티티와의 직접적인 관계 매핑을 하지 않으며, 상태 추적에 필요한 최소 필드만 유지합니다.

---

### 6. Enum 정의 (`JobStatus`)

비동기 작업의 상태를 나타내는 Enum 클래스를 정의합니다.

| **Enum 값** | **설명** |
| --- | --- |
| **`PROCESSING`** | 작업이 백그라운드에서 실행 중 |
| **`SUCCESS`** | 작업이 성공적으로 완료됨 |
| **`FAILURE`** | 작업 실행 중 오류가 발생하여 실패함 |
