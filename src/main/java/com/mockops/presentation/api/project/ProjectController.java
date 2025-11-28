package com.mockops.presentation.api.project;

import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.domain.project.service.ProjectService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ProjectMemberService projectMemberService;

    /**
     * 프로젝트 생성
     * POST /api/v1/projects
     */
    @PostMapping
    public ResponseEntity<UnifiedResponse<ProjectDetailResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @RequestAttribute("userId") Long currentUserId
    ) {
        log.info("프로젝트 생성 요청: userId={}, name={}", currentUserId, request.name());

        // 프로젝트 생성
        ProjectDetailResponse response = projectService.createProjectWithDetails(
                request.name(),
                request.description(),
                currentUserId,
                request.slackWebhookUrl()
        );

        // 생성자를 OWNER로 자동 추가
        projectMemberService.addProjectMember(response.id(), currentUserId, MemberRole.OWNER);

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
            @RequestAttribute("userId") Long currentUserId
    ) {
        log.info("내 프로젝트 목록 조회: userId={}, pageable={}", currentUserId, pageable);

        ProjectPageResponse response = projectService.getMyProjects(currentUserId, pageable);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 상세 조회
     * GET /api/v1/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<UnifiedResponse<ProjectDetailResponse>> getProject(
            @PathVariable Long projectId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        log.info("프로젝트 상세 조회: projectId={}, userId={}", projectId, currentUserId);

        ProjectDetailResponse response = projectService.getProjectDetails(projectId, currentUserId);
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 정보 수정
     * PATCH /api/v1/projects/{projectId}
     */
    @PatchMapping("/{projectId}")
    public ResponseEntity<UnifiedResponse<ProjectDetailResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @RequestAttribute("userId") Long currentUserId
    ) {
        log.info("프로젝트 수정 요청: projectId={}, userId={}", projectId, currentUserId);

        ProjectDetailResponse response = projectService.updateProjectWithDetails(
                projectId,
                currentUserId,
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
            @RequestAttribute("userId") Long currentUserId
    ) {
        log.info("프로젝트 삭제 요청: projectId={}, userId={}", projectId, currentUserId);

        projectService.deleteProject(projectId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}
