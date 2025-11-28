package com.mockops.domain.project.entity;

import com.mockops.domain.project.role.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectMember 엔티티 테스트")
class ProjectMemberTest {

    @Test
    @DisplayName("ProjectMember 객체를 생성할 수 있다")
    void createProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        MemberRole memberRole = MemberRole.DEVELOPER;

        // when
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(memberRole)
                .build();

        // then
        assertThat(projectMember).isNotNull();
        assertThat(projectMember.getProjectId()).isEqualTo(projectId);
        assertThat(projectMember.getUserId()).isEqualTo(userId);
        assertThat(projectMember.getMemberRole()).isEqualTo(memberRole);
    }

    @Test
    @DisplayName("멤버 역할을 변경할 수 있다")
    void updateMemberRole() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.VIEWER)
                .build();

        MemberRole newRole = MemberRole.DEVELOPER;

        // when
        projectMember.updateRole(newRole);

        // then
        assertThat(projectMember.getMemberRole()).isEqualTo(newRole);
    }

    @Test
    @DisplayName("OWNER 역할을 가진 ProjectMember를 생성할 수 있다")
    void createOwnerProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        MemberRole memberRole = MemberRole.OWNER;

        // when
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(memberRole)
                .build();

        // then
        assertThat(projectMember).isNotNull();
        assertThat(projectMember.getMemberRole()).isEqualTo(MemberRole.OWNER);
    }

    @Test
    @DisplayName("MANAGER 역할을 가진 ProjectMember를 생성할 수 있다")
    void createManagerProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        MemberRole memberRole = MemberRole.MANAGER;

        // when
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(userId)
                .memberRole(memberRole)
                .build();

        // then
        assertThat(projectMember).isNotNull();
        assertThat(projectMember.getMemberRole()).isEqualTo(MemberRole.MANAGER);
    }
}
