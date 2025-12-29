package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.role.Role;
import com.mockops.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectMemberService 테스트")
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private com.mockops.domain.user.service.UserService userService;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    @Test
    @DisplayName("ID로 프로젝트 멤버를 조회할 수 있다")
    void getProjectMemberById() {
        // given
        Long memberId = 1L;
        ProjectMember member = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();

        given(projectMemberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        ProjectMember foundMember = projectMemberService.getProjectMemberById(memberId);

        // then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getMemberRole()).isEqualTo(MemberRole.DEVELOPER);
        verify(projectMemberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getProjectMemberByIdNotFound() {
        // given
        given(projectMemberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectMemberService.getProjectMemberById(1L))
                .isInstanceOf(BusinessException.class);
        verify(projectMemberRepository).findById(1L);
    }

    @Test
    @DisplayName("프로젝트 ID와 사용자 ID로 멤버를 조회할 수 있다")
    void getProjectMemberByProjectIdAndUserId() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(MemberRole.DEVELOPER)
                .build();

        given(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .willReturn(Optional.of(member));

        // when
        ProjectMember foundMember = projectMemberService.getProjectMemberByProjectIdAndUserId(projectId, userId);

        // then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getProjectId()).isEqualTo(projectId);
        assertThat(foundMember.getUserId()).isEqualTo(userId);
        verify(projectMemberRepository).findByProjectIdAndUserId(projectId, userId);
    }

    @Test
    @DisplayName("프로젝트 ID로 멤버 목록을 조회할 수 있다")
    void getProjectMembersByProjectId() {
        // given
        Long projectId = 1L;
        List<ProjectMember> members = List.of(
                ProjectMember.builder().projectId(projectId).userId(2L).memberRole(MemberRole.OWNER).build(),
                ProjectMember.builder().projectId(projectId).userId(3L).memberRole(MemberRole.DEVELOPER).build()
        );

        given(projectMemberRepository.findByProjectId(projectId)).willReturn(members);

        // when
        List<ProjectMember> foundMembers = projectMemberService.getProjectMembersByProjectId(projectId);

        // then
        assertThat(foundMembers).hasSize(2);
        verify(projectMemberRepository).findByProjectId(projectId);
    }

    @Test
    @DisplayName("사용자 ID로 참여 중인 프로젝트 멤버 목록을 조회할 수 있다")
    void getProjectMembersByUserId() {
        // given
        Long userId = 2L;
        List<ProjectMember> members = List.of(
                ProjectMember.builder().projectId(1L).userId(userId).memberRole(MemberRole.OWNER).build(),
                ProjectMember.builder().projectId(3L).userId(userId).memberRole(MemberRole.DEVELOPER).build()
        );

        given(projectMemberRepository.findByUserId(userId)).willReturn(members);

        // when
        List<ProjectMember> foundMembers = projectMemberService.getProjectMembersByUserId(userId);

        // then
        assertThat(foundMembers).hasSize(2);
        verify(projectMemberRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("프로젝트 멤버를 추가할 수 있다")
    void addProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        MemberRole memberRole = MemberRole.DEVELOPER;

        User user = User.builder()
                .nickname("user")
                .email("email@naver.com")
                .role(Role.USER)
                .build();

        ProjectMember member = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(memberRole)
                .build();

        given(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)).willReturn(false);
        given(projectMemberRepository.save(any(ProjectMember.class))).willReturn(member);
        given(userService.getUserById(any(Long.class))).willReturn(user);

        // when
        ProjectMember addedMember = projectMemberService.addProjectMember(projectId, userId, memberRole);

        // then
        assertThat(addedMember).isNotNull();
        assertThat(addedMember.getProjectId()).isEqualTo(projectId);
        assertThat(addedMember.getUserId()).isEqualTo(userId);
        assertThat(addedMember.getMemberRole()).isEqualTo(memberRole);
        verify(projectMemberRepository).existsByProjectIdAndUserId(projectId, userId);
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    @DisplayName("이미 존재하는 멤버 추가 시 예외가 발생한다")
    void addProjectMemberDuplicated() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        given(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> projectMemberService.addProjectMember(projectId, userId, MemberRole.DEVELOPER))
                .isInstanceOf(BusinessException.class);
        verify(projectMemberRepository).existsByProjectIdAndUserId(projectId, userId);
    }

    @Test
    @DisplayName("멤버 역할을 변경할 수 있다")
    void updateMemberRole() {
        // given
        Long memberId = 1L;
        MemberRole newRole = MemberRole.MANAGER;

        ProjectMember member = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();

        given(projectMemberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        ProjectMember updatedMember = projectMemberService.updateMemberRole(memberId, newRole);

        // then
        assertThat(updatedMember.getMemberRole()).isEqualTo(newRole);
        verify(projectMemberRepository).findById(memberId);
    }

    @Test
    @DisplayName("프로젝트 멤버를 제거할 수 있다")
    void removeProjectMember() {
        // given
        Long memberId = 1L;
        Long currentUserId = 1L;
        Long projectId = 1L;
        ProjectMember targetMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();
        ProjectMember currentUserMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(currentUserId)
                .memberRole(MemberRole.MANAGER) // MANAGER 권한으로 설정
                .build();

        given(projectMemberRepository.findById(memberId)).willReturn(Optional.of(targetMember));
        given(projectMemberRepository.findByProjectIdAndUserId(projectId, currentUserId))
                .willReturn(Optional.of(currentUserMember));

        // when
        projectMemberService.removeProjectMember(memberId, currentUserId);

        // then
        verify(projectMemberRepository).findById(memberId);
        verify(projectMemberRepository).findByProjectIdAndUserId(projectId, currentUserId);
        verify(projectMemberRepository).delete(targetMember);
    }

    @Test
    @DisplayName("특정 역할의 프로젝트 멤버 존재 여부를 확인할 수 있다")
    void isProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        MemberRole memberRole = MemberRole.OWNER;

        given(projectMemberRepository.existsByProjectIdAndUserIdAndMemberRole(projectId, userId, memberRole))
                .willReturn(true);

        // when
        boolean exists = projectMemberService.isProjectMember(projectId, userId, memberRole);

        // then
        assertThat(exists).isTrue();
        verify(projectMemberRepository).existsByProjectIdAndUserIdAndMemberRole(projectId, userId, memberRole);
    }
}
