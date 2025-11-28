package com.mockops.domain.project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjectCorsOrigin 엔티티 테스트")
class ProjectCorsOriginTest {

    @Test
    @DisplayName("ProjectCorsOrigin 객체를 생성할 수 있다")
    void createProjectCorsOrigin() {
        // given
        Long projectId = 1L;
        String originUrl = "http://localhost:3000";

        // when
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(projectId)
                .originUrl(originUrl)
                .build();

        // then
        assertThat(corsOrigin).isNotNull();
        assertThat(corsOrigin.getProjectId()).isEqualTo(projectId);
        assertThat(corsOrigin.getOriginUrl()).isEqualTo(originUrl);
    }

    @Test
    @DisplayName("HTTPS Origin URL로 ProjectCorsOrigin을 생성할 수 있다")
    void createCorsOriginWithHttpsUrl() {
        // given
        Long projectId = 1L;
        String originUrl = "https://example.com";

        // when
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(projectId)
                .originUrl(originUrl)
                .build();

        // then
        assertThat(corsOrigin).isNotNull();
        assertThat(corsOrigin.getOriginUrl()).isEqualTo(originUrl);
    }

    @Test
    @DisplayName("포트 번호를 포함한 Origin URL로 ProjectCorsOrigin을 생성할 수 있다")
    void createCorsOriginWithPort() {
        // given
        Long projectId = 1L;
        String originUrl = "http://localhost:8080";

        // when
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(projectId)
                .originUrl(originUrl)
                .build();

        // then
        assertThat(corsOrigin).isNotNull();
        assertThat(corsOrigin.getOriginUrl()).isEqualTo(originUrl);
    }
}
