# entity_domain_server

## 📄 entity_domain_server.md

### 1. 엔티티 개요 (`DomainServer`)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `DomainServer` |
| **테이블명** | `domain_servers` |
| **설명** | 하나의 도메인 서비스(마이크로 서비스) 단위를 나타내며, 헬스 체크 URL 및 상태 정보를 저장합니다. |
| **상속** | `BaseEntity` 상속 (공통 필드: `createdAt`, `updatedAt` 자동 포함) |
| **사용 기술** | Lombok (`@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`), Spring Data JPA (`@Entity`, `@Table`) |
| **생성 정책** | **`@Builder` 패턴** 사용. PK (`id`)와 BaseEntity 필드는 빌더에서 제외됩니다. |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입**      | **설명** | **제약 조건 및 JPA 매핑**                                          |
| --- | --- |----------------| --- |-------------------------------------------------------------|
| **`id`** | `Long` | `BIGINT`       | PK (고유 식별자) | `@Id`, `@GeneratedValue(strategy = IDENTITY)`               |
| **`projectId`** | `Long` | `BIGINT`       | **소속 프로젝트(`Project`)의 ID** (FK 역할) | `@Column(nullable = false, name = "project_id")`            |
| **`name`** | `String` | `VARCHAR(100)` | 도메인 서버 이름 (예: `user-service`) | `@Column(nullable = false)`                                 |
| **`status`** | `ServerStatus` (Enum) | `VARCHAR`      | 현재 개발/운영 상태 | `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)` |
| **`healthCheckUrl`** | `String` | `VARCHAR(255)` | 실제 서버의 헬스 체크 API URL | `@Column(nullable = true)`                                  |
| **`healthCheckInterval`** | `String`  | `VARCHAR(3)`   | 실제 서버의 헬스 체크 주기 | `@Column(nullable = false, length = 3, DEFAULT '10m'")`     |
| **`lastCheckedAt`** | `Instant` | `TIMESTAMP`    | 헬스 체크가 마지막으로 수행된 일시 | `@Column(nullable = true)`                                  |
| **`isHealthCheckActive`** | `Boolean` | `BOOLEAN`      | 헬스 체크 기능 활성화 여부 (true/false) | `@Column(nullable = false)`                                 |

---

### 3. 생성자 및 빌더 패턴 상세

- **기본 생성자:** `@NoArgsConstructor(access = PROTECTED)` 사용.
- **빌더 패턴:** 빌더 인자는 `id`, `createdAt`, `updatedAt`, `lastCheckedAt` 필드를 제외한 **`projectId`, `name`, `status`, `healthCheckUrl`, `isHealthCheckActive`*을 포함합니다.

---

### 4. 관계 정의 (Relationships)

**단방향 외래 키 필드 전략 반영:**

`DomainServer`는 **`Project` ID**를 단순 `Long` 타입 필드로 관리합니다.

| **대상 엔티티** | **관계** | **매핑 방식** | **설명** |
| --- | --- | --- | --- |
| **`Project`** | Many-to-One | **매핑하지 않음** | `projectId` 필드를 통해 ID만 저장. |
| **`MockApi`** | One-to-Many | **필드 미포함** | 필요 시 `MockApiRepository`를 통해 `serverId`로 조회하여 사용합니다. |

### 5. 📢 공통 연관관계 매핑 전략 지침 (중요)

> [One-to-Many / 역참조 필드 지연 매핑 전략]
>
> 1. **초기 단계:** `DomainServer` 엔티티 내에 `@OneToMany` 관계를 나타내는 **`List<T>` 필드를 선언하지 않습니다.**
> 2. **데이터 접근:** 하위 목록 데이터가 필요할 경우, **`DomainServer` ID**를 이용하여 각 하위 엔티티의 **Repository를 통해 직접 조회**하는 방식으로 처리합니다.
> 3. **향후 매핑:** 코드 복잡성 감소, N+1 문제 회피, 그리고 성능 이점이 명확해질 때만, `@OneToMany` 필드와 매핑 코드를 **추가**합니다.

---

### 6. Enum 정의 (`ServerStatus`)

도메인 서버의 현재 상태를 나타내는 Enum 클래스를 정의합니다.

| **Enum 값** | **설명** |
| --- | --- |
| **`MOCKING`** | 개발 중이며 Mock 응답 제공 중 |
| **`PENDING`** | 배포가 완료되었으나 헬스 체크 시작 전 대기 상태 |
| **`DEPLOYED`** | 배포가 완료되었고 헬스 체크 결과 정상을 반환 중 |
| **`ERROR`** | 배포 완료 후 헬스 체크 결과 오류(4xx, 5xx 또는 타임아웃) 발생 |