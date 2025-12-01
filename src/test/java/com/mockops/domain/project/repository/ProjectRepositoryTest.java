package com.mockops.domain.project.repository;

import com.mockops.config.TestRedisConfig;
import com.mockops.domain.project.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestRedisConfig.class)
@DisplayName("ProjectRepository 테스트")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("프로젝트를 저장할 수 있다")
    void saveProject() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Test Description")
                .ownerId(1L)
                .slackWebhookUrl("https://hooks.slack.com/test")
                .build();

        // when
        Project savedProject = projectRepository.save(project);

        // then
        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getName()).isEqualTo("TestProject");
    }

    @Test
    @DisplayName("사용자 ID와 프로젝트 이름으로 조회할 수 있다")
    void findByOwnerIdAndName() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Test Description")
                .ownerId(1L)
                .build();
        projectRepository.save(project);

        // when
        Optional<Project> foundProject = projectRepository.findByOwnerIdAndName(1L, "TestProject");

        // then
        assertThat(foundProject).isPresent();
        assertThat(foundProject.get().getName()).isEqualTo("TestProject");
        assertThat(foundProject.get().getDescription()).isEqualTo("Test Description");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 빈 Optional을 반환한다")
    void findByOwnerIdAndNameNotFound() {
        // when
        Optional<Project> foundProject = projectRepository.findByOwnerIdAndName(1L, "NonExistentProject");

        // then
        assertThat(foundProject).isEmpty();
    }

    @Test
    @DisplayName("사용자별로 프로젝트 이름 존재 여부를 확인할 수 있다")
    void existsByOwnerIdAndName() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Test Description")
                .ownerId(1L)
                .build();
        projectRepository.save(project);

        // when
        boolean exists = projectRepository.existsByOwnerIdAndName(1L, "TestProject");
        boolean notExists = projectRepository.existsByOwnerIdAndName(1L, "NonExistentProject");
        boolean differentOwner = projectRepository.existsByOwnerIdAndName(2L, "TestProject");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
        assertThat(differentOwner).isFalse(); // 다른 사용자의 같은 이름 프로젝트는 별개
    }

    @Test
    @DisplayName("다른 사용자는 같은 이름의 프로젝트를 생성할 수 있다")
    void allowSameNameForDifferentOwners() {
        // given
        Project project1 = Project.builder()
                .name("MyProject")
                .ownerId(1L)
                .build();
        Project project2 = Project.builder()
                .name("MyProject")
                .ownerId(2L)
                .build();

        // when
        Project saved1 = projectRepository.save(project1);
        Project saved2 = projectRepository.save(project2);

        // then
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
        assertThat(saved1.getName()).isEqualTo(saved2.getName());
        assertThat(saved1.getOwnerId()).isNotEqualTo(saved2.getOwnerId());
    }

    @Test
    @DisplayName("프로젝트를 삭제할 수 있다")
    void deleteProject() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Test Description")
                .ownerId(1L)
                .build();
        Project savedProject = projectRepository.save(project);

        // when
        projectRepository.deleteById(savedProject.getId());

        // then
        Optional<Project> foundProject = projectRepository.findById(savedProject.getId());
        assertThat(foundProject).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 정보를 수정할 수 있다")
    void updateProject() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Old Description")
                .ownerId(1L)
                .slackWebhookUrl("https://hooks.slack.com/old")
                .build();
        Project savedProject = projectRepository.save(project);

        // when
        savedProject.updateProjectInfo("New Description", "https://hooks.slack.com/new");
        projectRepository.flush();

        // then
        Project foundProject = projectRepository.findById(savedProject.getId()).orElseThrow();
        assertThat(foundProject.getDescription()).isEqualTo("New Description");
        assertThat(foundProject.getSlackWebhookUrl()).isEqualTo("https://hooks.slack.com/new");
    }
}
