# tech_req_bulk_mock_creation

## Bulk Mock Creation Strategy: OpenAPI File Upload

### 1. 💡 목적 (Purpose)

사용자가 대규모 Mock API 구성을 위해 Swagger(OpenAPI) 명세 파일을 업로드하면, 이를 파싱하여 서비스 내부 Mock 데이터로 일괄 생성하고 저장하는 비동기 처리 메커니즘을 정의합니다.

### 2. 🔑 핵심 고려 사항 (Core Considerations)

- **비동기 처리**: 대용량 파일 파싱 및 DB 삽입 작업은 장시간이 소요되므로, Spring @Async 기반의 비동기 Worker를 활용하여 클라이언트에게 빠른 응답(Non-Blocking)을 보장해야 합니다.
- **성능 최적화**: JPA의 N+1 INSERT 문제를 해결하기 위해 JDBC Batching을 사용하여 데이터베이스 I/O 성능을 극대화해야 합니다.
- **파일 형식**: OpenAPI 3.0 명세서에서 추출 가능한 표준 형식인 YAML 또는 JSON을 입력으로 허용합니다.
- **상태 추적**: 사용자는 업로드한 파일의 파싱 및 Mock 생성 **진행 상태(Processing Status)**를 확인할 수 있어야 합니다.

### 3. ⚙️ 아키텍처 및 처리 흐름 (Architecture & Flow)

1. **파일 업로드:** 클라이언트가 YAML/JSON 파일을 서버의 REST API 엔드포인트로 업로드합니다.
2. **작업 등록 및 이벤트 발행(동기):
   - Job Entity를 생성하고 상태를 `PROCESSING`으로 초기화합니다.
   - 클라이언트에게 **Job ID와 HTTP 202 (Accepted) 상태 코드**를 즉시 응답합니다.
   - **`BulkCreationEvent`**를 발행하여 작업을 전용 스레드 풀로 위임합니다.
3. **비동기 Worker 실행:** `MockApiBulkWorker`의 `@EventListener` 및 `@Async("bulkTaskExecutor")` 메서드가 이벤트를 전용 스레드 풀에서 수신하여 실행합니다.
4. **Mock 데이터 생성 및 Bulk 삽입:** 
    - Worker는 파일을 파싱합니다.
    - **Bulk Select**를 통해 기존 API 경로를 **메모리 내 Set**에 로드하고, 이를 기준으로 파싱된 API의 중복을 필터링합니다. 
    - **JDBC Batch Insert**를 통해 중복이 없는 데이터만 DB에 일괄 삽입합니다. (N+1 INSERT 문제 해결)
5. **상태 업데이트:** 모든 처리가 완료되면 `JobTrackingService`를 통해 작업 상태를 `SUCCESS` 또는 `FAILURE`로 업데이트합니다.

### 4. 📝 지원되는 OpenAPI 스펙 영역 (Supported Parsing Scope)

우리는 Mock API의 **경로(Path)**, **HTTP 메서드**, **요청/응답 스키마**를 추출하는 데 집중합니다.

| 추출 대상 | OpenAPI 필드 (JSON Path) | Mock API 엔티티 매핑 |
| --- | --- | --- |
| **경로** | `paths/{/path}` | `endpointPath` |
| **HTTP 메서드** | `paths/{/path}/{method}` | `httpMethod` (GET, POST 등) |
| **요청 본문 스키마** | `paths/{/path}/{method}/requestBody/content/application/json/schema` | `MockRequestSchema` |
| **응답 본문 스키마** | `paths/{/path}/{method}/responses/{statusCode}/content/application/json/schema` | `MockResponseSchema` |
| **응답 예시** | `paths/{/path}/{method}/responses/{statusCode}/content/application/json/example` | `MockResponseData` (Mock의 실제 응답 데이터) |

### 5. 📂 지원되는 파일 형식 예시 (Supported File Format Examples)

사용자에게 이 양식만 파싱이 가능함을 명확히 고지합니다.

### A. YAML (OpenAPI 3.0) 예시

```
openapi: 3.0.0
info:
  title: MockAPI Sample Specification
  version: v1.0.0
paths:
  /users:
    get:
      tags:
        - User
      summary: 사용자 목록 조회
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
              example:
                - id: 1
                  name: "Kim Mock"
                  email: "kim@mockops.com"

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string

```

### B. JSON (OpenAPI 3.0) 예시

```
{
  "openapi": "3.0.0",
  "info": {
    "title": "MockAPI Sample Specification",
    "version": "v1.0.0"
  },
  "paths": {
    "/products": {
      "post": {
        "summary": "새 상품 등록",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/ProductRequest" }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Created",
            "content": {
              "application/json": {
                "example": { "productId": 101, "status": "success" }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ProductRequest": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "price": { "type": "number" }
        }
      }
    }
  }
}

```

### 6. 🛠️ 기술 구현 상세 (Technical Implementation Details)

#### 6.1. 비동기 작업 위임 및 스레드 풀 관리 (Spring Event & @Async)

- **작업 위임**: Controller는 `ApplicationEventPublisher`를 통해 `BulkCreationEvent`를 발행하며, **Non-Blocking 호출**로 작업을 위임합니다.
- **스레드 풀**: `@EnableAsync`와 `ThreadPoolTaskExecutor` 기반의 전용 스레드 풀 (`bulkTaskExecutor`)을 구성하여 장시간 I/O 작업을 분리합니다.
- **Rejected Policy (안정성 확보)**: 큐와 스레드 풀이 모두 꽉 찼을 경우, 웹 서버 스레드(Caller)의 블로킹을 막기 위해 **`ThreadPoolExecutor.AbortPolicy`**를 사용합니다. 이는 작업을 즉시 거부하고 예외를 발생시켜, 웹 서버 스레드의 고갈을 방지하고 서비스 전체의 안정성을 유지합니다.

#### 6.2. 대용량 조회 최적화 (Bulk Select for Duplicates)

- `MockApiRepository`를 통해 해당 `serverId`의 모든 기존 API의 **Method와 Path 조합**을 **단 한 번의 쿼리(Bulk Select)**로 조회합니다. 이 정보를 **메모리 내 `Set<String>`**에 저장한 후, 파싱된 데이터를 O(1) 시간 복잡도로 비교 필터링하여 DB 부하를 최소화합니다.

#### 6.3. 대용량 삽입 최적화 (JDBC Batching)

- `MockApiBulkInsertService**`를 도입하고, 영속성 컨텍스트를 우회하는 **J`dbcTemplate`**의 `batchUpdate` 기능을 사용합니다. 중복이 제거된 Mock API 목록을 받아 DB에 **단일 배치 요청**으로 전송하여 네트워크 왕복 횟수를 획기적으로 줄이고 대량 삽입 성능을 극대화합니다.