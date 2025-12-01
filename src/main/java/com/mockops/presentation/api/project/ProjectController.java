package com.mockops.presentation.api.project;

import com.mockops.domain.project.service.ProjectService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     * POST /api/v1/projects
     */
    @PostMapping
    public ResponseEntity<UnifiedResponse<ProjectCreateResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("프로젝트 생성 요청: userId={}, name={}", userId, request.name());

        ProjectCreateResponse response = projectService.createProject(
                request.name(),
                request.description(),
                userId,
                request.slackWebhookUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UnifiedResponse.success(response));
    }

    /**
     * 내 프로젝트 목록 조회 (페이지 기반)
     * GET /api/v1/projects
     */
    @GetMapping
    public ResponseEntity<UnifiedResponse<ProjectPageResponse>> getMyProjects(
            Pageable pageable,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("내 프로젝트 목록 조회: userId={}, pageable={}", userId, pageable);

        ProjectPageResponse response = projectService.getMyProjects(userId, pageable);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 상세 조회
     * GET /api/v1/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<UnifiedResponse<ProjectDetailResponse>> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("프로젝트 상세 조회: projectId={}, userId={}", projectId, userId);

        ProjectDetailResponse response = projectService.getProjectDetails(projectId, userId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 정보 수정
     * PATCH /api/v1/projects/{projectId}
     */
    @PatchMapping("/{projectId}")
    public ResponseEntity<UnifiedResponse<ProjectUpdateResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("프로젝트 수정 요청: projectId={}, userId={}", projectId, userId);

        ProjectUpdateResponse response = projectService.updateProjectWithDetails(
                projectId,
                userId,
                request.description(),
                request.slackWebhookUrl()
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 삭제
     * DELETE /api/v1/projects/{projectId}
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("프로젝트 삭제 요청: projectId={}, userId={}", projectId, userId);

        projectService.deleteProject(projectId, userId);

        return ResponseEntity.noContent().build();
    }
}
