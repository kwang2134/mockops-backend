package com.mockops.presentation.api.mock;

import com.mockops.domain.mock.service.DomainServerService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.mock.docs.DomainServerDocs;
import com.mockops.presentation.api.mock.dto.domainserver.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 도메인 서버 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DomainServerController implements DomainServerDocs {

    private final DomainServerService domainServerService;

    /**
     * 프로젝트의 서버 목록 조회
     * GET /api/v1/projects/{projectId}/servers
     */
    @Override
    @GetMapping("/projects/{projectId}/servers")
    public ResponseEntity<UnifiedResponse<Page<DomainServerSimpleResponse>>> getServersByProject(
        @PathVariable Long projectId,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("프로젝트의 서버 목록 조회: projectId={}, userId={}", projectId, userId);

        Page<DomainServerSimpleResponse> response = domainServerService.getServersByProjectWithResponse(
            projectId, userId, pageable
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 서버 상세 조회
     * GET /api/v1/servers/{serverId}
     */
    @Override
    @GetMapping("/servers/{serverId}")
    public ResponseEntity<UnifiedResponse<DomainServerResponse>> getServer(
        @PathVariable Long serverId,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("서버 상세 조회: serverId={}, userId={}", serverId, userId);

        DomainServerResponse response = domainServerService.getServerWithResponse(serverId, userId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 서버 생성
     * POST /api/v1/projects/{projectId}/servers
     */
    @Override
    @PostMapping("/projects/{projectId}/servers")
    public ResponseEntity<UnifiedResponse<DomainServerCreateResponse>> createServer(
        @PathVariable Long projectId,
        @Valid @RequestBody DomainServerCreateRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("서버 생성 요청: projectId={}, name={}, slug={}, userId={}",
            projectId, request.name(), request.slug(), userId);

        DomainServerCreateResponse response = domainServerService.createServerWithResponse(
            projectId,
            request.name(),
            request.slug(),
            request.healthCheckUrl(),
            request.healthCheckInterval(),
            userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UnifiedResponse.success(response));
    }

    /**
     * 서버 정보 수정
     * PUT /api/v1/servers/{serverId}
     */
    @Override
    @PutMapping("/servers/{serverId}")
    public ResponseEntity<UnifiedResponse<DomainServerUpdateResponse>> updateServer(
        @PathVariable Long serverId,
        @Valid @RequestBody DomainServerUpdateRequest request,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("서버 수정 요청: serverId={}, userId={}", serverId, userId);

        DomainServerUpdateResponse response = domainServerService.updateServerWithResponse(
            serverId,
            request.name(),
            request.healthCheckUrl(),
            request.healthCheckInterval(),
            request.status(),
            request.isHealthCheckActive(),
            userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 서버 삭제
     * DELETE /api/v1/servers/{serverId}
     */
    @Override
    @DeleteMapping("/servers/{serverId}")
    public ResponseEntity<Void> deleteServer(
        @PathVariable Long serverId,
        @AuthenticationPrincipal Long userId
    ) {
        log.info("서버 삭제 요청: serverId={}, userId={}", serverId, userId);

        domainServerService.deleteServer(serverId, userId);

        return ResponseEntity.noContent().build();
    }
}
