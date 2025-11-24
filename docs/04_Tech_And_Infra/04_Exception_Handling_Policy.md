# 예외 관리

## 🛡️ exception_handling.md: 최종 예외 처리 구조 명세

### 1. 🎯 목표 및 개요

- **목표:** 모든 예외를 **`GlobalExceptionHandler`*에서 일괄 처리하고, **`ErrorResponse`** 포맷을 사용하여 일관성 있는 오류 응답을 제공합니다.
- **핵심:** **`ErrorCode`** Enum을 통해 HTTP 상태, 오류 코드, 기본 메시지를 통합 관리합니다.

---

### 2. 🧱 핵심 구성 요소 정의

### A. 오류 응답 구조 (`ErrorResponse`)

클라이언트에게 반환할 최종 JSON 응답 구조입니다. `success` 필드를 **`false`로 고정**하여 오류 응답임을 명시합니다.

| **필드명** | **타입** | **설명** | **값** |
| --- | --- | --- | --- |
| `success` | boolean | 요청 성공 여부 | `false` (고정) |
| `code` | String | 에러 코드 (Enum 이름) | `ErrorCode.name()` |
| `message` | String | 에러 메시지 (상세 오류 내용) | `ErrorCode.getMessage()` 또는 상세 메시지 |
| `status`  | Integer | 응답 HTTP 상태 코드 | `ErrorCode.getHttpStatus()` |
| `path`  | String | 오류가 발생한 경로 (디버깅 목적) | 요청 URI (예: /api/v1/projects/1) |

> Factory Method 역할:
>
> - `from(ErrorCode)`: 표준 `ErrorCode`와 메시지를 사용합니다.
> - `of(String message)`: 코드가 없는 일반적인 오류 메시지 (예: `@Valid` 실패 등)에 사용됩니다.
> - `of(ErrorCode, String message)`: 표준 `ErrorCode`를 사용하지만, 메시지를 재정의할 때 사용됩니다.

### B. 커스텀 예외 클래스 (`BusinessException`)

제공해주신 `ErrorCode` Enum 내부에서 생성(Factory Method)되며, 예외 타입(`ErrorType`)을 포함합니다. (기존 `BusinessException` 역할을 대체합니다.)

- **상속:** `RuntimeException` 상속.
- **필드:** `ErrorCode`, `ErrorType` (도메인/서비스).
- **사용:** `throw ErrorCode.PROJECT_NOT_FOUND.domainException();` 와 같이 사용됩니다.

### C. 예외 코드 정의 (`ErrorCode` Enum)

모든 예외 상황을 중앙 관리하는 Enum입니다.

| **Enum 그룹** | **예시 코드** | **HttpStatus** | **설명** |
| --- | --- | --- | --- |
| **시스템** | `INTERNAL_SERVER_ERROR` | 500 | 서버 오류 |
|  | `BAD_REQUEST` | 400 | 유효하지 않은 요청 |
| **인증/Auth** | `INVALID_PROVIDER` | 400 | 유효하지 않은 OAuth 제공자 |
|  | `UNAUTHORIZED` | 401 | 로그인 필요 |
|  | `INVALID_TOKEN` | 401 | 유효하지 않은 토큰 |
|  | `EXPIRED_TOKEN` | 401 | 토큰 만료 |
| **프로젝트** | `PROJECT_NOT_FOUND` | 404 | 프로젝트 미발견 |
| **...** | *추가될 모든 도메인 예외* |  |  |

> Factory Method 역할:
>
> - `serviceException()`, `domainException()`: `ErrorCode`와 `ErrorType`을 지정하여 `BusinessException`을 생성합니다.
> - `serviceException(String detail)`, `domainException(String detail)`: 기본 메시지 대신 상세 메시지를 포함하여 `BusinessException`을 생성합니다.

---

### 3. 🖥️ 글로벌 예외 핸들러 동작 (GlobalExceptionHandler)

- **클래스:** `GlobalExceptionHandler` (`@RestControllerAdvice` 적용)
- **처리 로직:**
    1. `@ExceptionHandler(BusinessException.class)` 메서드를 정의합니다.
    2. `BusinessException`에서 `ErrorCode`를 추출합니다.
    3. `ErrorCode`의 `HttpStatus`와 메시지를 사용하여 **`ErrorResponse`** 객체를 생성하여 반환합니다.