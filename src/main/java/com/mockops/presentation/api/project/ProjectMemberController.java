package com.mockops.presentation.api.project;

import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.docs.ProjectMemberDocs;
import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberInviteRequest;
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
    private final UserService userService;

    /**
     * 팀원 초대/추가 (VIEWER 고정)
     * POST /api/v1/projects/{projectId}/members
     * 웹 서비스 알림 구현 후 초대 기능 (메일 발송 x)
     */
    @Override
    @PostMapping
    public ResponseEntity<UnifiedResponse<ProjectMemberResponse>> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody MemberInviteRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("팀원 초대 요청: projectId={}, email={}, userId={}",
                projectId, request.email(), userId);

        // 이메일로 사용자 조회
        User invitedUser = userService.getUserByEmail(request.email());

        // VIEWER 역할로 멤버 추가
        ProjectMemberResponse response = projectMemberService.inviteMemberWithDetails(
                projectId,
                userId,
                invitedUser.getId(),
                MemberRole.VIEWER
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
