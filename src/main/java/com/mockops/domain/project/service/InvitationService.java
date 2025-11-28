package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.Invitation;
import com.mockops.domain.project.entity.InvitationStatus;
import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.repository.InvitationRepository;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.security.JwtProvider;
import com.mockops.infrastructure.mail.MailService;
import com.mockops.presentation.api.project.dto.InvitationCreateRequest;
import com.mockops.presentation.api.project.dto.InvitationCreateResponse;
import com.mockops.presentation.api.project.dto.InvitationInfo;
import com.mockops.presentation.api.project.dto.PagedInvitationListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * 프로젝트 초대 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final MailService mailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 프로젝트 초대 생성 및 이메일 발송
     */
    @Transactional
    public InvitationCreateResponse createInvitation(Long projectId, InvitationCreateRequest request, Long inviterId) {
        // 1. 권한 검증: MANAGER 이상만 초대 가능
        projectMemberService.validateMemberPermission(projectId, inviterId, MemberRole.MANAGER);

        // 2. 프로젝트 존재 확인
        Project project = projectService.getProjectById(projectId);

        // 3. 초대자 정보 조회
        User inviter = userService.getUserById(inviterId);

        // 4. 이미 프로젝트 멤버인지 확인 (이메일로 사용자 찾기)
        User invitedUser = userService.findByEmail(request.email()).orElse(null);
        if (invitedUser != null) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, invitedUser.getId());
            if (isMember) {
                throw ErrorCode.PROJECT_MEMBER_DUPLICATED.serviceException(
                    "이미 프로젝트에 참여 중인 사용자입니다. email=" + request.email()
                );
            }
        }

        // 5. 이미 PENDING 상태의 초대가 있는지 확인
        boolean hasExistingInvitation = invitationRepository.existsByProjectIdAndInvitedEmailAndStatus(
            projectId, request.email(), InvitationStatus.PENDING
        );
        if (hasExistingInvitation) {
            throw ErrorCode.INVITATION_ALREADY_EXISTS.serviceException(
                "이미 해당 이메일로 초대장이 발송되었습니다. email=" + request.email()
            );
        }

        // 6. 초대장 생성 (만료 시간: 7일)
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        // 임시로 초대장 저장 (토큰은 나중에 업데이트)
        Invitation invitation = Invitation.builder()
            .projectId(projectId)
            .inviterId(inviterId)
            .invitedEmail(request.email())
            .tokenValue("temp") // 임시 값
            .expiresAt(expiresAt)
            .memberRole(request.memberRole() != null ? request.memberRole() : MemberRole.VIEWER)
            .build();

        invitation = invitationRepository.save(invitation);

        // 7. JWT 토큰 생성
        String token = jwtProvider.generateInvitationToken(invitation.getId(), request.email(), projectId);

        // 8. 토큰 해시값 저장
        String tokenHash = hashToken(token);
        Invitation savedInvitation = invitationRepository.findById(invitation.getId())
            .orElseThrow(() -> ErrorCode.INVITATION_NOT_FOUND.serviceException());

        // 리플렉션을 사용하지 않고 새 객체 생성
        Invitation updatedInvitation = Invitation.builder()
            .projectId(savedInvitation.getProjectId())
            .inviterId(savedInvitation.getInviterId())
            .invitedEmail(savedInvitation.getInvitedEmail())
            .tokenValue(tokenHash)
            .expiresAt(savedInvitation.getExpiresAt())
            .memberRole(savedInvitation.getMemberRole())
            .build();

        invitationRepository.delete(savedInvitation);
        invitation = invitationRepository.save(updatedInvitation);

        // 9. 초대 링크 생성
        String invitationLink = frontendUrl + "/invitations/accept?token=" + token;

        // 10. 이메일 발송
        try {
            mailService.sendProjectInvitationEmail(
                request.email(),
                inviter.getNickname(),
                project.getName(),
                invitationLink,
                7 // 7일
            );
            log.info("초대 이메일 발송 성공: projectId={}, email={}", projectId, request.email());
        } catch (Exception e) {
            log.error("초대 이메일 발송 실패: projectId={}, email={}, error={}",
                projectId, request.email(), e.getMessage());
            // 이메일 발송 실패해도 초대장은 생성된 상태로 유지
        }

        return InvitationCreateResponse.from(invitation);
    }

    /**
     * 프로젝트 초대 목록 조회
     */
    public PagedInvitationListResponse getInvitations(Long projectId, InvitationStatus status,
                                                     Pageable pageable, Long currentUserId) {
        // 권한 검증: MANAGER 이상만 조회 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.MANAGER);

        Page<Invitation> invitationPage;
        if (status != null) {
            invitationPage = invitationRepository.findByProjectIdAndStatus(projectId, status, pageable);
        } else {
            invitationPage = invitationRepository.findByProjectId(projectId, pageable);
        }

        Page<InvitationInfo> infoPage = invitationPage.map(InvitationInfo::from);
        return PagedInvitationListResponse.from(infoPage);
    }

    /**
     * 초대 취소
     */
    @Transactional
    public void cancelInvitation(Long projectId, Long invitationId, Long currentUserId) {
        // 권한 검증: MANAGER 이상만 취소 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.MANAGER);

        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> ErrorCode.INVITATION_NOT_FOUND.serviceException(
                "존재하지 않는 초대장입니다. invitationId=" + invitationId
            ));

        // 프로젝트 ID 검증
        if (!invitation.getProjectId().equals(projectId)) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                "해당 프로젝트의 초대장이 아닙니다. projectId=" + projectId
            );
        }

        // PENDING 상태인지 확인
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw ErrorCode.INVITATION_CANCELED.serviceException(
                "이미 처리된 초대장입니다. status=" + invitation.getStatus()
            );
        }

        invitation.cancel();
        invitationRepository.save(invitation);

        log.info("초대 취소 완료: invitationId={}, projectId={}", invitationId, projectId);
    }

    /**
     * 초대 수락 처리
     */
    @Transactional
    public void acceptInvitation(String token, Long currentUserId) {
        // 1. JWT 토큰 검증
        jwtProvider.validateInvitationToken(token);

        // 2. 토큰에서 정보 추출
        Long invitationId = jwtProvider.getInvitationIdFromToken(token);
        String email = jwtProvider.getEmailFromInvitationToken(token);
        Long projectId = jwtProvider.getProjectIdFromInvitationToken(token);

        // 3. 초대장 조회
        Invitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> ErrorCode.INVITATION_NOT_FOUND.serviceException(
                "존재하지 않는 초대장입니다. invitationId=" + invitationId
            ));

        // 4. 초대장 상태 확인
        if (!invitation.isValid()) {
            if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
                throw ErrorCode.INVITATION_ALREADY_ACCEPTED.serviceException();
            } else if (invitation.getStatus() == InvitationStatus.CANCELED) {
                throw ErrorCode.INVITATION_CANCELED.serviceException();
            } else {
                throw ErrorCode.INVITATION_EXPIRED.serviceException();
            }
        }

        // 5. 현재 사용자의 이메일과 초대 이메일 일치 확인
        User currentUser = userService.getUserById(currentUserId);
        if (!currentUser.getEmail().equals(email)) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                "초대받은 이메일과 현재 로그인한 이메일이 다릅니다."
            );
        }

        // 6. 이미 프로젝트 멤버인지 확인
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId);
        if (isMember) {
            throw ErrorCode.PROJECT_MEMBER_DUPLICATED.serviceException(
                "이미 프로젝트에 참여 중입니다."
            );
        }

        // 7. 프로젝트 멤버 추가
        ProjectMember newMember = ProjectMember.builder()
            .projectId(projectId)
            .userId(currentUserId)
            .memberRole(invitation.getMemberRole())
            .build();

        projectMemberRepository.save(newMember);

        // 8. 초대장 상태 변경
        invitation.accept();
        invitationRepository.save(invitation);

        log.info("초대 수락 완료: invitationId={}, userId={}, projectId={}",
            invitationId, currentUserId, projectId);
    }

    /**
     * 토큰 해시 생성 (SHA-256)
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
