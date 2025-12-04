package com.mockops.presentation.api.project;

import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.docs.ProjectMemberDocs;
import com.mockops.presentation.api.project.dto.MemberInviteAcceptRequest;
import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberListResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberRoleUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 멤버 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController implements ProjectMemberDocs {

    private final ProjectMemberService projectMemberService;

    /**
     * 알림을 통한 초대 수락
     * POST /api/v1/projects/{projectId}/members
     *
     * 웹 서비스를 통해 알림으로 받은 초대를 수락하는 메서드
     * 로그인된 사용자가 알림의 초대를 수락하여 프로젝트 멤버로 참여
     */
    @Override
    @PostMapping
    public ResponseEntity<UnifiedResponse<ProjectMemberResponse>> acceptMember(
            @PathVariable Long projectId,
            @Valid @RequestBody MemberInviteAcceptRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("알림을 통한 초대 수락 요청: projectId={}, userId={}, requestUserId={}",
                projectId, userId, request.userId());

        ProjectMemberResponse response = projectMemberService.acceptInvitationFromNotification(
                projectId,
                request.userId(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UnifiedResponse.success(response));
    }

    /**
     * 프로젝트 팀원 목록 조회 (커서 기반 페이징)
     * GET /api/v1/projects/{projectId}/members?size=20&cursorId=123
     */
    @Override
    @GetMapping
    public ResponseEntity<UnifiedResponse<MemberListResponse>> getMembers(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long cursorId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("팀원 목록 조회: projectId={}, size={}, cursorId={}", projectId, size, cursorId);

        MemberListResponse response = projectMemberService.getMembersWithPagination(
                projectId,
                userId,
                cursorId,
                size
        );
        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 팀원 역할 변경 (OWNER 제외)
     * PATCH /api/v1/projects/{projectId}/members/{memberId}/role
     */
    @Override
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<UnifiedResponse<ProjectMemberResponse>> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("팀원 역할 변경 요청: projectId={}, memberId={}, newRole={}",
                projectId, memberId, request.memberRole());

        ProjectMemberResponse response = projectMemberService.updateMemberRoleWithDetails(
                memberId,
                userId,
                request.memberRole()
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 팀원 제외 (OWNER 제외)
     * DELETE /api/v1/projects/{projectId}/members/{memberId}
     */
    @Override
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("팀원 제외 요청: projectId={}, memberId={}", projectId, memberId);

        projectMemberService.removeProjectMember(memberId, userId);

        return ResponseEntity.noContent().build();
    }
}
