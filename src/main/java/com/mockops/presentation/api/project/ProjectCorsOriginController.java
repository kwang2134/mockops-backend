package com.mockops.presentation.api.project;

import com.mockops.domain.project.service.ProjectCorsOriginService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.projectcorsorigin.CorsOriginRequest;
import com.mockops.presentation.api.project.dto.projectcorsorigin.CorsOriginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CORS Origin 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/cors")
@RequiredArgsConstructor
public class ProjectCorsOriginController {

    private final ProjectCorsOriginService corsOriginService;

    /**
     * 허용 Origin 추가
     * POST /api/v1/projects/{projectId}/cors
     */
    @PostMapping
    public ResponseEntity<UnifiedResponse<CorsOriginResponse>> addCorsOrigin(
            @PathVariable Long projectId,
            @Valid @RequestBody CorsOriginRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("CORS Origin 추가 요청: projectId={}, originUrl={}", projectId, request.originUrl());

        CorsOriginResponse response = corsOriginService.addCorsOriginWithResponse(
                projectId,
                userId,
                request.originUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UnifiedResponse.success(response));
    }

    /**
     * 허용 Origin 목록 조회
     * GET /api/v1/projects/{projectId}/cors
     */
    @GetMapping
    public ResponseEntity<UnifiedResponse<List<CorsOriginResponse>>> getCorsOrigins(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("CORS Origin 목록 조회: projectId={}", projectId);

        List<CorsOriginResponse> response = corsOriginService.getCorsOriginsWithResponse(projectId, userId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 허용 Origin 삭제
     * DELETE /api/v1/projects/{projectId}/cors/{corsId}
     */
    @DeleteMapping("/{corsId}")
    public ResponseEntity<Void> deleteCorsOrigin(
            @PathVariable Long projectId,
            @PathVariable Long corsId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("CORS Origin 삭제 요청: projectId={}, corsId={}", projectId, corsId);

        corsOriginService.deleteCorsOrigin(corsId, userId);

        return ResponseEntity.noContent().build();
    }
}
