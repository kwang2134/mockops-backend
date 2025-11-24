# tech_req_auth_security

## 🔑 `tech_req_auth_security.md`: 인증 및 보안 기술 요구사항

### 1. 🌐 OAuth2 제공자 및 확장 정책

| **구분** | **요구사항** | **상세 정책** |
| --- | --- | --- |
| **초기 지원 Provider** | **Google** 및 **GitHub** | 개발자 도구의 핵심 사용자층을 고려하여 초기 서비스를 시작합니다. |
| **확장 정책** | **추후 카카오 및 네이버 확장 가능성 있음** | `AuthProvider` 엔티티 및 관련 로직은 확장을 염두에 두고 설계하며, 새로운 Provider 추가 시 **최소한의 코드 변경**이 발생하도록 유연하게 구현합니다. |
| **구현 기술** | Spring Security의 OAuth2 Client 기능을 사용하여 **Authorization Code Grant Type**으로 구현합니다. |  |

---

### 2. 🛡️ 토큰 관리 및 수명 (TTL)

MockOps는 높은 보안성과 로그아웃 기능을 제공하기 위해 **Hybrid 방식**의 토큰 관리를 채택합니다.

| **항목** | **값** | **정책 및 구현 시 고려 사항** |
| --- | --- | --- |
| **Access Token TTL** | **30분** | API 호출 시 인증에 사용되며, 만료 시 **Refresh Token**을 통해 갱신합니다. |
| **Refresh Token TTL** | **2주** | 장기간 사용을 허용합니다. 만료 시 사용자에게 재로그인을 유도합니다. |
| **토큰 저장소** | **`AuthProvider` 엔티티** | Refresh Token 자체 또는 해시 값을 해당 사용자의 **`AuthProvider` 레코드에 저장(Stateful)**하여, 서버 측에서 강제 만료(로그아웃) 처리가 가능하도록 합니다. |
| **갱신 전략** | **재발급(Rotation)** | Access Token 갱신 요청 시, 보안 강화를 위해 **기존 Refresh Token을 폐기**하고 **새로운 Refresh Token**을 발급하여 클라이언트에게 전달합니다. |

---

### 3. 📦 토큰 전달 및 보안 방식 (Transport & Security)

클라이언트와 서버 간의 토큰 전달은 **XSS(Cross-Site Scripting) 및 CSRF(Cross-Site Request Forgery) 공격**을 방어하도록 설정합니다.

| **토큰** | **전달 방식** | **보안 속성 및 구현** |
| --- | --- | --- |
| **Access Token** | **HTTP Header** (`Authorization: Bearer <Token>`) | 모든 REST API 요청 시 사용. **프론트엔드 JS에서 접근 및 관리**하여 헤더에 포함합니다. |
| **Refresh Token** | **HttpOnly Cookie** | 1. **`HttpOnly`**: JS 접근 불가능, XSS 공격 방어. 2. **`Secure`**: HTTPS 통신에서만 전송. 3. **`SameSite=Strict` 또는 `Lax`**: CSRF 공격 방어. |
| **인증 필터** | **Custom Filter Chain** | Spring Security Filter Chain 내에 Access Token 검증 필터를 구성하고, Refresh Token 갱신 요청을 처리하는 별도의 엔드포인트를 구성합니다. |

---

## 🛡️ 4. CORS 동적 처리 및 캐싱 (Dynamic CORS Handling)

MockOps는 프로젝트별로 등록된 Origin 목록을 기반으로 CORS 정책을 동적으로 적용하며, 성능 최적화를 위해 Redis 캐시를 사용합니다.

### 4.1. 요구사항 및 처리 개요

| **구분** | **요구사항** | **설명** |
| --- | --- | --- |
| **정책 적용 범위** | 모든 Mock API (`/mock/**`) 요청 | Mock API로 들어오는 모든 요청(특히 `OPTIONS` Preflight 요청)에 대해 적용됩니다. |
| **처리 목표** | DB 접근 최소화 및 고성능 보장 | 요청이 들어올 때마다 DB를 조회하는 대신, Redis 캐시를 활용하여 오버헤드를 최소화합니다. |
| **필수 데이터** | `ProjectCorsOrigin` 엔티티 | 프로젝트 ID를 키로 하여 허용된 `originUrl` 목록을 DB에 저장하고, 이를 캐시의 소스(Source)로 사용합니다. |

### 4.2. 구현 아키텍처 및 메커니즘

| **구분** | **상세 구현 방안** | **처리 주체** |
| --- | --- | --- |
| **처리 계층** | **Custom `CorsConfigurationSource`** | Spring Security의 필터 체인에서 실행되는 **`CorsFilter`**에 주입되어 동적 정책을 제공합니다. (Controller 호출 이전에 처리) |
| **캐시 키** | `CORS:PROJECT:{projectId}` | 각 프로젝트 ID를 키로 하여 해당 프로젝트의 허용 Origin URL 목록(`List<String>`)을 Redis에 저장합니다. |
| **로드 로직** | Cache-Aside 전략 | 1. 요청에서 `projectId` 식별. 2. Redis에서 `CORS:PROJECT:{projectId}` 조회. 3. **Cache Hit** 시, 즉시 Origin 목록 반환. 4. **Cache Miss** 시, DB(`ProjectCorsOrigin`) 조회 후 Redis에 저장하고 반환. |
| **캐시 무효화** | 명시적 이벤트 기반 제거 | 프로젝트 설정에서 허용 Origin을 추가, 수정, 삭제하는 **관리 API 호출 성공 시**, 해당 `projectId`에 대한 Redis 캐시(`CORS:PROJECT:{projectId}`)를 **명시적으로 제거(Evict)**하여 즉시 최신 정보를 반영합니다. |

### 4.3. Preflight 요청 처리

- **`OPTIONS` 메서드 처리:** `CorsFilter`는 `OPTIONS` 메서드로 들어오는 Preflight 요청을 감지하고, 동적 정책에 따라 Origin 유효성을 검사합니다.
- **유효성 검증:** 요청 Origin이 Redis/DB에서 로드된 목록에 포함되어 있다면, 필요한 `Access-Control-*` 헤더를 설정하여 **즉시 응답**하고, `DispatcherServlet` 이하의 모든 처리를 중단합니다.