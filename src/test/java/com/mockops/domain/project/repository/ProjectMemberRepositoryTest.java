package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.role.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProjectMemberRepository 테스트")
class ProjectMemberRepositoryTest {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Test
    @DisplayName("프로젝트 멤버를 저장할 수 있다")
    void saveProjectMember() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();

        // when
        ProjectMember savedMember = projectMemberRepository.save(projectMember);

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getProjectId()).isEqualTo(1L);
        assertThat(savedMember.getUserId()).isEqualTo(2L);
        assertThat(savedMember.getMemberRole()).isEqualTo(MemberRole.DEVELOPER);
    }

    @Test
    @DisplayName("프로젝트 ID로 멤버 목록을 조회할 수 있다")
    void findByProjectId() {
        // given
        ProjectMember member1 = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.OWNER)
                .build();
        ProjectMember member2 = ProjectMember.builder()
                .projectId(1L)
                .userId(3L)
                .memberRole(MemberRole.DEVELOPER)
                .build();
        projectMemberRepository.save(member1);
        projectMemberRepository.save(member2);

        // when
        List<ProjectMember> members = projectMemberRepository.findByProjectId(1L);

        // then
        assertThat(members).hasSize(2);
        assertThat(members).extracting(ProjectMember::getUserId)
                .containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @DisplayName("사용자 ID로 참여 중인 프로젝트 멤버 목록을 조회할 수 있다")
    void findByUserId() {
        // given
        ProjectMember member1 = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();
        ProjectMember member2 = ProjectMember.builder()
                .projectId(3L)
                .userId(2L)
                .memberRole(MemberRole.VIEWER)
                .build();
        projectMemberRepository.save(member1);
        projectMemberRepository.save(member2);

        // when
        List<ProjectMember> members = projectMemberRepository.findByUserId(2L);

        // then
        assertThat(members).hasSize(2);
        assertThat(members).extracting(ProjectMember::getProjectId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("프로젝트 ID와 사용자 ID로 멤버를 조회할 수 있다")
    void findByProjectIdAndUserId() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();
        projectMemberRepository.save(projectMember);

        // when
        Optional<ProjectMember> foundMember = projectMemberRepository.findByProjectIdAndUserId(1L, 2L);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getMemberRole()).isEqualTo(MemberRole.DEVELOPER);
    }

    @Test
    @DisplayName("프로젝트 멤버 존재 여부를 확인할 수 있다")
    void existsByProjectIdAndUserId() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.DEVELOPER)
                .build();
        projectMemberRepository.save(projectMember);

        // when
        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(1L, 2L);
        boolean notExists = projectMemberRepository.existsByProjectIdAndUserId(1L, 999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 역할의 프로젝트 멤버 존재 여부를 확인할 수 있다")
    void existsByProjectIdAndUserIdAndMemberRole() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.OWNER)
                .build();
        projectMemberRepository.save(projectMember);

        // when
        boolean ownerExists = projectMemberRepository.existsByProjectIdAndUserIdAndMemberRole(
                1L, 2L, MemberRole.OWNER
        );
        boolean developerExists = projectMemberRepository.existsByProjectIdAndUserIdAndMemberRole(
                1L, 2L, MemberRole.DEVELOPER
        );

        // then
        assertThat(ownerExists).isTrue();
        assertThat(developerExists).isFalse();
    }

    @Test
    @DisplayName("프로젝트 멤버 역할을 변경할 수 있다")
    void updateMemberRole() {
        // given
        ProjectMember projectMember = ProjectMember.builder()
                .projectId(1L)
                .userId(2L)
                .memberRole(MemberRole.VIEWER)
                .build();
        ProjectMember savedMember = projectMemberRepository.save(projectMember);

        // when
        savedMember.updateRole(MemberRole.DEVELOPER);
        projectMemberRepository.flush();

        // then
        ProjectMember foundMember = projectMemberRepository.findById(savedMember.getId()).orElseThrow();
        assertThat(foundMember.getMemberRole()).isEqualTo(MemberRole.DEVELOPER);
    }
}
