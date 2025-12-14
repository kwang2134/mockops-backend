package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.Invitation;
import com.mockops.domain.project.entity.InvitationStatus;
import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.repository.InvitationRepository;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final InvitationRepository invitationRepository;
    private final UserService userService;

    public ProjectMember getProjectMemberById(Long memberId) {
        return projectMemberRepository.findById(memberId)
                .orElseThrow(() -> ErrorCode.PROJECT_MEMBER_NOT_FOUND.domainException(
                        "해당하는 프로젝트 멤버가 존재하지 않습니다. memberId=" + memberId
                ));
    }

    public ProjectMember getProjectMemberByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> ErrorCode.PROJECT_MEMBER_NOT_FOUND.domainException(
                        "해당하는 프로젝트 멤버가 존재하지 않습니다. projectId=" + projectId + ", userId=" + userId
                ));
    }

    public List<ProjectMember> getProjectMembersByProjectId(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    public List<ProjectMember> getProjectMembersByUserId(Long userId) {
        return projectMemberRepository.findByUserId(userId);
    }

    /**
     * 프로젝트 멤버 권한 검증
     * @throws com.mockops.global.exception.DomainException 권한이 없는 경우
     */
    public void validateMemberPermission(Long projectId, Long userId, MemberRole requiredRole) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> ErrorCode.PERMISSION_DENIED.domainException(
                        "프로젝트 멤버가 아닙니다. projectId=" + projectId + ", userId=" + userId
                ));

        if (!member.getMemberRole().hasPermission(requiredRole)) {
            throw ErrorCode.PERMISSION_DENIED.domainException(
                    "권한이 부족합니다. required=" + requiredRole + ", actual=" + member.getMemberRole()
            );
        }
    }

    /**
     * 프로젝트 소유자 검증
     */
    public void validateOwner(Long projectId, Long userId) {
        validateMemberPermission(projectId, userId, MemberRole.OWNER);
    }

    /**
     * Offset 기반 페이징으로 프로젝트 멤버 조회
     * 권한 순서로 정렬: OWNER -> MANAGER -> DEVELOPER -> VIEWER
     * 같은 권한 내에서는 ID 오름차순
     */
    public List<ProjectMember> getProjectMembersByProjectIdWithOffset(Long projectId, int offset, int size) {
        Pageable pageable = PageRequest.of(offset / size, size + 1); // hasNext 판단을 위해 1개 더 조회
        return projectMemberRepository.findByProjectIdOrderByMemberRoleAscIdAsc(projectId, pageable);
    }

    /**
     * 프로젝트 멤버 목록 조회 (Offset 기반 페이징) - Response DTO 반환
     * 권한: PROJECT_MEMBER 이상
     */
    public MemberListResponse getMembersWithPagination(Long projectId, Long currentUserId, Integer offset, int size) {
        // 권한 검증: 프로젝트 멤버 여부 확인
        validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        // offset이 null이면 0으로 처리
        int actualOffset = (offset == null) ? 0 : offset;

        // 데이터베이스 레벨에서 offset 기반 페이징 처리
        List<ProjectMember> members = getProjectMembersByProjectIdWithOffset(projectId, actualOffset, size);

        // hasNext 계산
        boolean hasNext = members.size() > size;
        List<ProjectMember> pagedMembers = hasNext
                ? members.subList(0, size)
                : members;

        // DTO 변환
        List<ProjectMemberResponse> memberResponses = pagedMembers.stream()
                .map(member -> {
                    User user = userService.getUserById(member.getUserId());
                    return ProjectMemberResponse.from(member, user.getNickname());
                })
                .collect(Collectors.toList());

        // nextOffset 계산
        Integer nextOffset = hasNext ? actualOffset + size : null;

        return new MemberListResponse(memberResponses, hasNext, nextOffset);
    }

    /**
     * 팀원 초대 - Response DTO 반환
     * 권한: PROJECT_MANAGER 이상
     */
    @Transactional
    public ProjectMemberResponse inviteMemberWithDetails(Long projectId, Long currentUserId, Long userId, MemberRole memberRole) {
        // 권한 검증: MANAGER 이상만 멤버 추가 가능
        validateMemberPermission(projectId, currentUserId, MemberRole.MANAGER);

        ProjectMember member = addProjectMember(projectId, userId, memberRole);
        User user = userService.getUserById(userId);
        return ProjectMemberResponse.from(member, user.getNickname());
    }

    /**
     * 팀원 역할 변경 - Response DTO 반환
     * 권한: PROJECT_MANAGER 이상 (OWNER 역할 변경 금지)
     */
    @Transactional
    public ProjectMemberResponse updateMemberRoleWithDetails(Long memberId, Long currentUserId, MemberRole newRole) {
        ProjectMember targetMember = getProjectMemberById(memberId);

        // 권한 검증: MANAGER 이상만 역할 변경 가능
        validateMemberPermission(targetMember.getProjectId(), currentUserId, MemberRole.MANAGER);

        // OWNER 역할 변경 금지
        if (targetMember.getMemberRole() == MemberRole.OWNER) {
            throw ErrorCode.PERMISSION_DENIED.domainException(
                    "OWNER 역할은 변경할 수 없습니다. memberId=" + memberId
            );
        }

        ProjectMember updatedMember = updateMemberRole(memberId, newRole);
        User user = userService.getUserById(updatedMember.getUserId());
        return ProjectMemberResponse.from(updatedMember, user.getNickname());
    }

    @Transactional
    public ProjectMember addProjectMember(Long projectId, Long userId, MemberRole memberRole) {
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ErrorCode.PROJECT_MEMBER_DUPLICATED.domainException(
                    "이미 프로젝트에 참여 중인 멤버입니다. projectId=" + projectId + ", userId=" + userId
            );
        }

        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(memberRole)
                .build();

        return projectMemberRepository.save(projectMember);
    }

    @Transactional
    public ProjectMember updateMemberRole(Long memberId, MemberRole memberRole) {
        ProjectMember projectMember = getProjectMemberById(memberId);
        projectMember.updateRole(memberRole);
        return projectMember;
    }

    /**
     * 팀원 제외
     * 권한: PROJECT_MANAGER 이상 (OWNER 제외 금지)
     */
    @Transactional
    public void removeProjectMember(Long memberId, Long currentUserId) {
        ProjectMember targetMember = getProjectMemberById(memberId);

        // 권한 검증: MANAGER 이상만 멤버 제외 가능
        validateMemberPermission(targetMember.getProjectId(), currentUserId, MemberRole.MANAGER);

        // OWNER 제외 금지
        if (targetMember.getMemberRole() == MemberRole.OWNER) {
            throw ErrorCode.PERMISSION_DENIED.domainException(
                    "OWNER는 제외할 수 없습니다. memberId=" + memberId
            );
        }

        projectMemberRepository.delete(targetMember);
    }

    public boolean isProjectMember(Long projectId, Long userId, MemberRole memberRole) {
        return projectMemberRepository.existsByProjectIdAndUserIdAndMemberRole(projectId, userId, memberRole);
    }

    /**
     * 알림을 통한 초대 수락
     * 웹 서비스를 통해 알림으로 받은 초대를 수락하여 프로젝트 멤버로 추가
     */
    @Transactional
    public ProjectMemberResponse acceptInvitationFromNotification(Long projectId, Long requestUserId, Long authenticatedUserId) {
        // 요청의 userId와 인증된 userId가 일치하는지 확인
        if (!authenticatedUserId.equals(requestUserId)) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                    "본인의 초대만 수락할 수 있습니다."
            );
        }

        // 해당 사용자의 이메일로 PENDING 상태의 초대장 조회
        User user = userService.getUserById(authenticatedUserId);
        List<Invitation> pendingInvitations = invitationRepository
                .findByProjectIdAndInvitedEmailAndStatus(projectId, user.getEmail(), InvitationStatus.PENDING);

        if (pendingInvitations.isEmpty()) {
            throw ErrorCode.INVITATION_NOT_FOUND.serviceException(
                    "해당 프로젝트의 초대장을 찾을 수 없습니다."
            );
        }

        // 가장 최신 초대장 사용
        Invitation invitation = pendingInvitations.get(0);

        // 초대장이 유효한지 확인
        if (!invitation.isValid()) {
            throw ErrorCode.INVITATION_EXPIRED.serviceException(
                    "만료되었거나 무효한 초대장입니다."
            );
        }

        // 프로젝트 멤버로 추가
        ProjectMember member = addProjectMember(
                projectId,
                authenticatedUserId,
                invitation.getMemberRole()
        );

        // 초대장 상태를 ACCEPTED로 변경 (더티 체킹으로 자동 UPDATE)
        invitation.accept();

        log.info("알림을 통한 초대 수락 완료: projectId={}, userId={}, invitationId={}",
                projectId, authenticatedUserId, invitation.getId());

        // Response 생성
        return ProjectMemberResponse.from(member, user.getNickname());
    }
}
