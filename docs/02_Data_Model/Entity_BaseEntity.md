# entity_base

이 파일은 JPA Auditing 설정을 포함하여 모든 엔티티가 상속받는 **공통 필드 및 정책**을 정의합니다.

## 📄 entity_base.md

### 1. 엔티티 개요 (BaseEntity)

| **항목** | **내용** |
| --- | --- |
| **클래스명** | `BaseEntity` |
| **테이블명** | 해당 없음 (테이블 생성 안 함) |
| **설명** | 모든 JPA 엔티티가 상속하는 공통 속성(시간 정보)을 정의하는 추상 클래스입니다. |
| **사용 기술** | Lombok (`@Getter`), Spring Data JPA (`@MappedSuperclass`, `@EntityListeners`) |
| **Auditing 설정** | JPA Auditing을 사용하여 생성 및 수정 시간을 자동으로 관리합니다. |
| **시간 정책** | 모든 시간 관리는 `java.time.Instant`를 사용한 **UTC(협정 세계시)** 기준으로 처리합니다. |

---

### 2. 필드 및 속성 정의

| **필드명** | **자바 타입** | **DB 타입** | **설명** | **JPA 매핑 및 속성** |
| --- | --- | --- | --- | --- |
| **`createdAt`** | `Instant` | `TIMESTAMP` | 엔티티 **최초 생성 일시** | `@CreatedDate` (자동 삽입), `@Column(updatable = false)` (수정 불가) |
| **`updatedAt`** | `Instant` | `TIMESTAMP` | 엔티티 **최종 수정 일시** | `@LastModifiedDate` (자동 업데이트) |

---

### 3. 클래스 구현 상세 정책

| **정책 항목** | **적용 어노테이션 및 상세** | **이유** |
| --- | --- | --- |
| **상속 매핑** | `@MappedSuperclass` | 이 클래스의 필드를 상속받는 하위 엔티티의 테이블에 매핑되도록 지시합니다. |
| **Auditing 활성화** | `@EntityListeners(AuditingEntityListener.class)` | Spring Data JPA의 Auditing 기능을 활성화하여 `createdAt`과 `updatedAt` 필드가 자동으로 채워지도록 합니다. |
| **접근 제한** | `abstract class` | 이 클래스 자체는 객체로 생성되지 않고 오직 상속 목적으로만 사용됨을 명시합니다. |
| **데이터 접근** | `@Getter` | 필드에 대한 읽기(Getter) 접근만 허용합니다. (setter는 Auditing 기능이 담당) |

**Note:** 이 `BaseEntity`를 사용하려면, 메인 애플리케이션 클래스에 **`@EnableJpaAuditing`** 어노테이션이 반드시 적용되어야 합니다.