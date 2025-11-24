# tech_req_mocking_caching

## 🚀 동적 Mocking 요청 및 캐싱 기술 요구사항

### 1. ⚙️ 핵심 라우팅 기술: Custom HandlerMapping

| **구분** | **요구사항** | **상세 구현 방안** | **처리 주체** |
| --- | --- | --- | --- |
| **처리 방식** | Custom HandlerMapping 기반 라우팅 | Spring MVC의 `HandlerMapping` 인터페이스를 구현하여, **`/mock/**` 요청에 대해 Spring의 일반 컨트롤러 매핑 과정을 우회**하고 전용 핸들러로 즉시 분기 처리합니다. | **`MockopsHandlerMapping`** (구현 클래스) |
| **라우팅 Prefix** | `/mock/{projectId}/{serverName}/**` | 모든 Mocking 요청은 이 Prefix로 들어오며, `projectId`와 `serverName`을 경로 변수로 추출하여 **Redis/DB 조회 키**로 사용합니다. | `MockopsHandlerMapping` |
| **URL 매칭 기술** | AntPathMatcher 및 Redis 활용 | **Redis에 캐싱된** Mock API의 경로 패턴(예: `/users/{id}`)과 클라이언트의 실제 요청 경로를 **`AntPathMatcher`**를 활용하여 비교합니다. | `MockopsHandlerMapping` |
| **경로 변수 추출** | 매칭 성공 시 필수 추출 | 라우팅에 필요한 `projectId`, `serverName` 외에, 사용자 정의 경로 변수 값({id}: 123)을 추출합니다. | `MockopsHandlerMapping` |
| **추출 변수 용도** | **패턴 매칭 검증 및 로깅** | 추출된 변수 값은 **동적 응답 생성에는 사용하지 않으며**, 엔드포인트 패턴과의 정확한 매칭을 검증하고 상세 로깅을 위한 데이터로만 활용됩니다. | `MockApiHandlerAdapter` (로깅 시점) |
| **핸들러 반환** | 전용 핸들러 반환 | 매칭 성공 시, Mock 데이터 정보를 포함하는 **전용 `MockApiHandler`** 객체를 반환하여 `DispatcherServlet`이 **`MockApiHandlerAdapter`**를 호출하도록 유도합니다. | `MockopsHandlerMapping` |

---

### 2. 💾 Mock API 데이터 캐싱 전략 (Redis 기반)

Mock API의 읽기 성능을 극대화하기 위해 Redis를 사용하여 **Write-Through/Write-Back** 정책을 적용합니다.

### A. 캐시 구조 (Key & Value)

| **항목** | **요구사항** | **상세 설명** |
| --- | --- | --- |
| **저장소** | **Redis (분산 캐시)** | 고성능과 수평 확장을 고려하여 Redis를 캐싱 저장소로 사용합니다. |
| **캐싱 키 (`Key`)** | **`{projectId}:{serverName}:{httpMethod}:{endpointPath}`** | Project ID, Server Name, HTTP Method, 최종 API 경로를 조합한 키를 사용하여 **O(1) 시간 복잡도**로 Mock 데이터를 조회합니다. |
| **캐싱 밸류 (`Value`)** | **JSON 문자열** | `MockCacheDto`와 같은 Java 객체를 **`GenericJackson2JsonRedisSerializer`** 등을 사용하여 JSON 문자열로 직렬화하여 저장합니다. |
| **Value 포함 요소** | `statusCode` (Integer), `responseBody` (String), `responseHeaders` (Map<String, String>) | 클라이언트에게 응답하기 위해 필요한 모든 정보를 포함하여 DB 조회 없이 즉시 응답을 구성합니다. |

### B. 캐시 무효화/갱신 정책

| **이벤트** | **요구사항** | **정책 및 구현** |
| --- | --- | --- |
| **읽기 (Mock API 호출)** | **Cache-Aside/Look-Aside** | 1. **Redis 조회** → 2. **Redis Miss 시 DB 조회** → 3. **DB 조회 성공 시 Redis에 캐시** → 4. **응답** 순서로 처리합니다. |
| **쓰기 (Mock API 수정/삭제)** | **Cache Eviction (즉시 무효화)** | `MockApi` 데이터가 DB에서 **수정되거나 삭제될 때**, 해당 Mock API의 캐싱 키를 Redis에서 **즉시 삭제(Evict)** 하여 데이터 불일치를 방지합니다. |

---

### 3. 🔎 Mock 데이터 조회 흐름

1. Mock 요청 수신 (`/mock/{pId}/{sName}/path`)
2. 요청 정보 (PId, SName, Method, Path) 추출
3. **Redis (캐시) 조회 시도** (Key: `{pId}:{sName}:METHOD:path` 조합)
4. **Cache Hit:** 캐시 밸류(JSON)를 파싱하여 `statusCode`와 `responseBody`로 즉시 응답 구성 → 반환.
5. **Cache Miss:** DB `MockApi` 테이블 조회 시도 (AntPathMatcher를 이용한 패턴 매칭)
6. **DB Hit:** 조회된 Mock 데이터를 Redis에 캐싱하고 → 응답 반환.
7. **DB Miss:** 404 Not Found 응답 반환.