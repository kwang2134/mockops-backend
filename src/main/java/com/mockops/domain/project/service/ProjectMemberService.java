package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.ProjectMember;
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
     * 커서 기반 페이징으로 프로젝트 멤버 조회
     */
    public List<ProjectMember> getProjectMembersByProjectIdWithCursor(Long projectId, Long cursorId, int size) {
        Pageable pageable = PageRequest.of(0, size + 1); // hasNext 판단을 위해 1개 더 조회

        if (cursorId == null) {
            return projectMemberRepository.findByProjectIdOrderByIdAsc(projectId, pageable);
        } else {
            return projectMemberRepository.findByProjectIdAndIdGreaterThanOrderByIdAsc(projectId, cursorId, pageable);
        }
    }

    /**
     * 프로젝트 멤버 목록 조회 (커서 기반 페이징) - Response DTO 반환
     * 권한: PROJECT_MEMBER 이상
     */
    public MemberListResponse getMembersWithPagination(Long projectId, Long currentUserId, Long cursorId, int size) {
        // 권한 검증: 프로젝트 멤버 여부 확인
        validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        // 데이터베이스 레벨에서 커서 기반 페이징 처리
        List<ProjectMember> members = getProjectMembersByProjectIdWithCursor(projectId, cursorId, size);

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

        // nextCursorId 계산
        Long nextCursorId = pagedMembers.isEmpty()
                ? null
                : pagedMembers.get(pagedMembers.size() - 1).getId();

        return new MemberListResponse(memberResponses, hasNext, nextCursorId);
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
}
