package com.mockops.domain.project.repository;

import com.mockops.config.TestJacksonConfig;
import com.mockops.config.TestRedisConfig;
import com.mockops.domain.project.entity.ProjectCorsOrigin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({TestJacksonConfig.class, TestRedisConfig.class})
@DisplayName("ProjectCorsOriginRepository 테스트")
class ProjectCorsOriginRepositoryTest {

    @Autowired
    private ProjectCorsOriginRepository projectCorsOriginRepository;

    @Test
    @DisplayName("CORS Origin을 저장할 수 있다")
    void saveCorsOrigin() {
        // given
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();

        // when
        ProjectCorsOrigin savedOrigin = projectCorsOriginRepository.save(corsOrigin);

        // then
        assertThat(savedOrigin).isNotNull();
        assertThat(savedOrigin.getId()).isNotNull();
        assertThat(savedOrigin.getProjectId()).isEqualTo(1L);
        assertThat(savedOrigin.getOriginUrl()).isEqualTo("http://localhost:3000");
    }

    @Test
    @DisplayName("프로젝트 ID로 CORS Origin 목록을 조회할 수 있다")
    void findByProjectId() {
        // given
        ProjectCorsOrigin origin1 = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();
        ProjectCorsOrigin origin2 = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("https://example.com")
                .build();
        projectCorsOriginRepository.save(origin1);
        projectCorsOriginRepository.save(origin2);

        // when
        List<ProjectCorsOrigin> origins = projectCorsOriginRepository.findByProjectId(1L);

        // then
        assertThat(origins).hasSize(2);
        assertThat(origins).extracting(ProjectCorsOrigin::getOriginUrl)
                .containsExactlyInAnyOrder("http://localhost:3000", "https://example.com");
    }

    @Test
    @DisplayName("프로젝트 ID와 Origin URL로 CORS Origin을 조회할 수 있다")
    void findByProjectIdAndOriginUrl() {
        // given
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();
        projectCorsOriginRepository.save(corsOrigin);

        // when
        Optional<ProjectCorsOrigin> foundOrigin = projectCorsOriginRepository
                .findByProjectIdAndOriginUrl(1L, "http://localhost:3000");

        // then
        assertThat(foundOrigin).isPresent();
        assertThat(foundOrigin.get().getOriginUrl()).isEqualTo("http://localhost:3000");
    }

    @Test
    @DisplayName("CORS Origin 존재 여부를 확인할 수 있다")
    void existsByProjectIdAndOriginUrl() {
        // given
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();
        projectCorsOriginRepository.save(corsOrigin);

        // when
        boolean exists = projectCorsOriginRepository.existsByProjectIdAndOriginUrl(
                1L, "http://localhost:3000"
        );
        boolean notExists = projectCorsOriginRepository.existsByProjectIdAndOriginUrl(
                1L, "http://localhost:8080"
        );

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("프로젝트의 모든 CORS Origin을 삭제할 수 있다")
    void deleteByProjectId() {
        // given
        ProjectCorsOrigin origin1 = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();
        ProjectCorsOrigin origin2 = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("https://example.com")
                .build();
        projectCorsOriginRepository.save(origin1);
        projectCorsOriginRepository.save(origin2);

        // when
        projectCorsOriginRepository.deleteByProjectId(1L);
        projectCorsOriginRepository.flush();

        // then
        List<ProjectCorsOrigin> origins = projectCorsOriginRepository.findByProjectId(1L);
        assertThat(origins).isEmpty();
    }

    @Test
    @DisplayName("개별 CORS Origin을 삭제할 수 있다")
    void deleteCorsOrigin() {
        // given
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();
        ProjectCorsOrigin savedOrigin = projectCorsOriginRepository.save(corsOrigin);

        // when
        projectCorsOriginRepository.deleteById(savedOrigin.getId());

        // then
        Optional<ProjectCorsOrigin> foundOrigin = projectCorsOriginRepository.findById(savedOrigin.getId());
        assertThat(foundOrigin).isEmpty();
    }
}
