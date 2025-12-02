package com.mockops.presentation.api.mock;

import com.mockops.domain.mock.service.MockApiService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.mock.docs.MockApiDocs;
import com.mockops.presentation.api.mock.dto.mockapi.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mock API 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MockApiController implements MockApiDocs {

    private final MockApiService mockApiService;

    /**
     * 서버의 Mock API 목록 조회 (커서 기반 페이징)
     * GET /api/v1/servers/{serverId}/mock-apis
     */
    @Override
    @GetMapping("/servers/{serverId}/mock-apis")
    public ResponseEntity<UnifiedResponse<MockApiListResponse>> getMockApisByServer(
        @PathVariable Long serverId,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("서버의 Mock API 목록 조회: serverId={}, cursor={}, size={}, userId={}",
            serverId, cursor, size, userId);

        MockApiListResponse response = mockApiService.getMockApisByServerWithResponse(
            serverId, cursor, size, userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * Mock API 상세 조회 (responseBody 포함)
     * GET /api/v1/mock-apis/{mockApiId}
     */
    @Override
    @GetMapping("/mock-apis/{mockApiId}")
    public ResponseEntity<UnifiedResponse<MockApiDetailResponse>> getMockApi(
        @PathVariable Long mockApiId,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 상세 조회: mockApiId={}, userId={}", mockApiId, userId);

        MockApiDetailResponse response = mockApiService.getMockApiByIdWithResponse(mockApiId, userId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * Mock API 생성
     * POST /api/v1/servers/{serverId}/mock-apis
     */
    @Override
    @PostMapping("/servers/{serverId}/mock-apis")
    public ResponseEntity<UnifiedResponse<MockApiCreateResponse>> createMockApi(
        @PathVariable Long serverId,
        @Valid @RequestBody MockApiCreateRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 생성 요청: serverId={}, name={}, method={}, path={}, userId={}",
            serverId, request.name(), request.httpMethod(), request.endpointPath(), userId);

        MockApiCreateResponse response = mockApiService.createMockApiWithResponse(
            serverId,
            request.name(),
            request.httpMethod(),
            request.endpointPath(),
            request.responseBody(),
            request.statusCode(),
            request.isActive(),
            userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UnifiedResponse.success(response));
    }

    /**
     * OpenAPI 스펙 파일 업로드를 통한 Mock API 일괄 생성
     * POST /api/v1/servers/{serverId}/mock-apis/upload
     */
    @Override
    @PostMapping("/servers/{serverId}/mock-apis/upload")
    public ResponseEntity<UnifiedResponse<MockApiBulkResponse>> uploadMockApis(
        @PathVariable Long serverId,
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 일괄 생성 요청: serverId={}, filename={}, userId={}",
            serverId, file.getOriginalFilename(), userId);

        MockApiBulkResponse response = mockApiService.createMockApisFromFile(serverId, file, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UnifiedResponse.success(response));
    }

    /**
     * Mock API 수정
     * PUT /api/v1/mock-apis/{mockApiId}
     */
    @Override
    @PutMapping("/mock-apis/{mockApiId}")
    public ResponseEntity<UnifiedResponse<MockApiUpdateResponse>> updateMockApi(
        @PathVariable Long mockApiId,
        @Valid @RequestBody MockApiUpdateRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 수정 요청: mockApiId={}, userId={}", mockApiId, userId);

        MockApiUpdateResponse response = mockApiService.updateMockApiWithResponse(
            mockApiId,
            request.name(),
            request.httpMethod(),
            request.endpointPath(),
            request.responseBody(),
            request.statusCode(),
            request.isActive(),
            userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * Mock API 삭제
     * DELETE /api/v1/mock-apis/{mockApiId}
     */
    @Override
    @DeleteMapping("/mock-apis/{mockApiId}")
    public ResponseEntity<Void> deleteMockApi(
        @PathVariable Long mockApiId,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 삭제 요청: mockApiId={}, userId={}", mockApiId, userId);

        mockApiService.deleteMockApi(mockApiId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Mock API 활성화 상태 토글
     * PATCH /api/v1/mock-apis/{mockApiId}/toggle
     */
    @Override
    @PatchMapping("/mock-apis/{mockApiId}/toggle")
    public ResponseEntity<UnifiedResponse<MockApiResponse>> toggleMockApiStatus(
        @PathVariable Long mockApiId,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("Mock API 상태 토글 요청: mockApiId={}, userId={}", mockApiId, userId);

        MockApiResponse response = mockApiService.toggleMockApiStatusWithResponse(mockApiId, userId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }
}
