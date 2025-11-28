package com.mockops.presentation.api.project;

import com.mockops.domain.project.entity.InvitationStatus;
import com.mockops.domain.project.service.InvitationService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.InvitationCreateRequest;
import com.mockops.presentation.api.project.dto.InvitationCreateResponse;
import com.mockops.presentation.api.project.dto.PagedInvitationListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 초대 관리 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * 팀원 초대 생성 및 발송
     * POST /api/v1/projects/{projectId}/invitations
     */
    @PostMapping("/api/v1/projects/{projectId}/invitations")
    public ResponseEntity<UnifiedResponse<InvitationCreateResponse>> createInvitation(
            @PathVariable Long projectId,
            @Valid @RequestBody InvitationCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("팀원 초대 요청: projectId={}, email={}, userId={}",
            projectId, request.email(), userId);

        InvitationCreateResponse response = invitationService.createInvitation(
            projectId, request, userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UnifiedResponse.success(response));
    }

    /**
     * 진행 중인 초대 목록 조회
     * GET /api/v1/projects/{projectId}/invitations
     */
    @GetMapping("/api/v1/projects/{projectId}/invitations")
    public ResponseEntity<UnifiedResponse<PagedInvitationListResponse>> getInvitations(
            @PathVariable Long projectId,
            @RequestParam(required = false) InvitationStatus status,
            Pageable pageable,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("초대 목록 조회: projectId={}, status={}, userId={}",
            projectId, status, userId);

        PagedInvitationListResponse response = invitationService.getInvitations(
            projectId, status, pageable, userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 초대 취소
     * DELETE /api/v1/projects/{projectId}/invitations/{invitationId}
     */
    @DeleteMapping("/api/v1/projects/{projectId}/invitations/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long projectId,
            @PathVariable Long invitationId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("초대 취소 요청: projectId={}, invitationId={}, userId={}",
            projectId, invitationId, userId);

        invitationService.cancelInvitation(projectId, invitationId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 초대 수락 처리 (Public endpoint)
     * GET /public/invitations/accept?token={token}
     *
     * 실제로는 프론트엔드에서 토큰을 받아 로그인 후 백엔드로 전달하는 방식
     * 여기서는 이미 로그인된 사용자가 토큰과 함께 요청하는 것으로 구현
     */
    @GetMapping("/api/v1/invitations/accept")
    public ResponseEntity<UnifiedResponse<String>> acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("초대 수락 요청: userId={}", userId);

        invitationService.acceptInvitation(token, userId);

        return ResponseEntity.ok(
            UnifiedResponse.success("초대를 성공적으로 수락했습니다.")
        );
    }
}
