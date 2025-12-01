package com.mockops.presentation.api.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Mock API 엔드포인트 (더미 컨트롤러)
 *
 * 실제 요청 처리는 MockOpsHandlerMapping에서 수행됩니다.
 * 이 컨트롤러는 다음 목적으로 존재합니다:
 *
 * 1. API 문서 자동 생성 (Swagger/OpenAPI)
 * 2. Spring Boot Actuator의 /mappings 엔드포인트에 표시
 * 3. Spring Security 설정의 명확성
 * 4. IDE 엔드포인트 검색 지원
 *
 * 주의: 이 컨트롤러의 메소드는 절대 실행되지 않습니다.
 *      MockOpsHandlerMapping이 우선순위(HIGHEST_PRECEDENCE)로 요청을 가로챕니다.
 */
@Slf4j
@RestController
@RequestMapping("/mock")
@Tag(name = "Public Mock API", description = "Mock API 요청 수신 엔드포인트 (Public)")
public class PublicMockController {

    /**
     * Mock API 요청 처리 (GET)
     *
     * 실제 처리: MockOpsHandlerMapping → MockApiHandlerAdapter
     *
     * @param projectId 프로젝트 ID
     * @param serverName 서버 이름
     */
    @Operation(
        summary = "Mock API 요청 (GET)",
        description = "등록된 Mock API로 GET 요청을 보냅니다. " +
                     "실제 처리는 MockOpsHandlerMapping에서 수행되며, " +
                     "이 메소드는 실행되지 않습니다."
    )
    @GetMapping("/{projectId}/{serverName}/**")
    public ResponseEntity<String> handleGetRequest(
        @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
        @Parameter(description = "서버 이름") @PathVariable String serverName
    ) {
        // 이 코드는 절대 실행되지 않습니다 (MockOpsHandlerMapping이 가로챔)
        log.error("PublicMockController.handleGetRequest 실행됨 - 이것은 오류입니다!");
        throw new IllegalStateException("이 메소드는 실행되어서는 안 됩니다. MockOpsHandlerMapping을 확인하세요.");
    }

    /**
     * Mock API 요청 처리 (POST)
     */
    @Operation(
        summary = "Mock API 요청 (POST)",
        description = "등록된 Mock API로 POST 요청을 보냅니다."
    )
    @PostMapping("/{projectId}/{serverName}/**")
    public ResponseEntity<String> handlePostRequest(
        @PathVariable Long projectId,
        @PathVariable String serverName
    ) {
        log.error("PublicMockController.handlePostRequest 실행됨 - 이것은 오류입니다!");
        throw new IllegalStateException("이 메소드는 실행되어서는 안 됩니다. MockOpsHandlerMapping을 확인하세요.");
    }

    /**
     * Mock API 요청 처리 (PUT)
     */
    @Operation(
        summary = "Mock API 요청 (PUT)",
        description = "등록된 Mock API로 PUT 요청을 보냅니다."
    )
    @PutMapping("/{projectId}/{serverName}/**")
    public ResponseEntity<String> handlePutRequest(
        @PathVariable Long projectId,
        @PathVariable String serverName
    ) {
        log.error("PublicMockController.handlePutRequest 실행됨 - 이것은 오류입니다!");
        throw new IllegalStateException("이 메소드는 실행되어서는 안 됩니다. MockOpsHandlerMapping을 확인하세요.");
    }

    /**
     * Mock API 요청 처리 (DELETE)
     */
    @Operation(
        summary = "Mock API 요청 (DELETE)",
        description = "등록된 Mock API로 DELETE 요청을 보냅니다."
    )
    @DeleteMapping("/{projectId}/{serverName}/**")
    public ResponseEntity<String> handleDeleteRequest(
        @PathVariable Long projectId,
        @PathVariable String serverName
    ) {
        log.error("PublicMockController.handleDeleteRequest 실행됨 - 이것은 오류입니다!");
        throw new IllegalStateException("이 메소드는 실행되어서는 안 됩니다. MockOpsHandlerMapping을 확인하세요.");
    }

    /**
     * Mock API 요청 처리 (PATCH)
     */
    @Operation(
        summary = "Mock API 요청 (PATCH)",
        description = "등록된 Mock API로 PATCH 요청을 보냅니다."
    )
    @PatchMapping("/{projectId}/{serverName}/**")
    public ResponseEntity<String> handlePatchRequest(
        @PathVariable Long projectId,
        @PathVariable String serverName
    ) {
        log.error("PublicMockController.handlePatchRequest 실행됨 - 이것은 오류입니다!");
        throw new IllegalStateException("이 메소드는 실행되어서는 안 됩니다. MockOpsHandlerMapping을 확인하세요.");
    }
}
