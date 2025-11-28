package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.ProjectCorsOrigin;
import com.mockops.domain.project.repository.ProjectCorsOriginRepository;
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
@DisplayName("ProjectCorsOriginService 테스트")
class ProjectCorsOriginServiceTest {

    @Mock
    private ProjectCorsOriginRepository projectCorsOriginRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private com.mockops.domain.project.infrastructure.CorsOriginCachePort corsOriginCachePort;

    @InjectMocks
    private ProjectCorsOriginService projectCorsOriginService;

    @Test
    @DisplayName("ID로 CORS Origin을 조회할 수 있다")
    void getCorsOriginById() {
        // given
        Long corsOriginId = 1L;
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();

        given(projectCorsOriginRepository.findById(corsOriginId)).willReturn(Optional.of(corsOrigin));

        // when
        ProjectCorsOrigin foundOrigin = projectCorsOriginService.getCorsOriginById(corsOriginId);

        // then
        assertThat(foundOrigin).isNotNull();
        assertThat(foundOrigin.getOriginUrl()).isEqualTo("http://localhost:3000");
        verify(projectCorsOriginRepository).findById(corsOriginId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getCorsOriginByIdNotFound() {
        // given
        given(projectCorsOriginRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectCorsOriginService.getCorsOriginById(1L))
                .isInstanceOf(BusinessException.class);
        verify(projectCorsOriginRepository).findById(1L);
    }

    @Test
    @DisplayName("프로젝트 ID로 CORS Origin 목록을 조회할 수 있다")
    void getCorsOriginsByProjectId() {
        // given
        Long projectId = 1L;
        List<ProjectCorsOrigin> origins = List.of(
                ProjectCorsOrigin.builder().projectId(projectId).originUrl("http://localhost:3000").build(),
                ProjectCorsOrigin.builder().projectId(projectId).originUrl("https://example.com").build()
        );

        given(projectCorsOriginRepository.findByProjectId(projectId)).willReturn(origins);

        // when
        List<ProjectCorsOrigin> foundOrigins = projectCorsOriginService.getCorsOriginsByProjectId(projectId);

        // then
        assertThat(foundOrigins).hasSize(2);
        verify(projectCorsOriginRepository).findByProjectId(projectId);
    }

    @Test
    @DisplayName("CORS Origin을 추가할 수 있다")
    void addCorsOrigin() {
        // given
        Long projectId = 1L;
        String originUrl = "http://localhost:3000";

        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(projectId)
                .originUrl(originUrl)
                .build();

        given(projectCorsOriginRepository.existsByProjectIdAndOriginUrl(projectId, originUrl)).willReturn(false);
        given(projectCorsOriginRepository.save(any(ProjectCorsOrigin.class))).willReturn(corsOrigin);

        // when
        ProjectCorsOrigin addedOrigin = projectCorsOriginService.addCorsOrigin(projectId, originUrl);

        // then
        assertThat(addedOrigin).isNotNull();
        assertThat(addedOrigin.getProjectId()).isEqualTo(projectId);
        assertThat(addedOrigin.getOriginUrl()).isEqualTo(originUrl);
        verify(projectCorsOriginRepository).existsByProjectIdAndOriginUrl(projectId, originUrl);
        verify(projectCorsOriginRepository).save(any(ProjectCorsOrigin.class));
    }

    @Test
    @DisplayName("이미 존재하는 CORS Origin 추가 시 예외가 발생한다")
    void addCorsOriginDuplicated() {
        // given
        Long projectId = 1L;
        String originUrl = "http://localhost:3000";
        given(projectCorsOriginRepository.existsByProjectIdAndOriginUrl(projectId, originUrl)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> projectCorsOriginService.addCorsOrigin(projectId, originUrl))
                .isInstanceOf(BusinessException.class);
        verify(projectCorsOriginRepository).existsByProjectIdAndOriginUrl(projectId, originUrl);
    }

    @Test
    @DisplayName("CORS Origin을 삭제할 수 있다")
    void deleteCorsOrigin() {
        // given
        Long corsOriginId = 1L;
        Long currentUserId = 1L;
        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(1L)
                .originUrl("http://localhost:3000")
                .build();

        given(projectCorsOriginRepository.findById(corsOriginId)).willReturn(Optional.of(corsOrigin));

        // when
        projectCorsOriginService.deleteCorsOrigin(corsOriginId, currentUserId);

        // then
        verify(projectCorsOriginRepository).findById(corsOriginId);
        verify(projectCorsOriginRepository).delete(corsOrigin);
    }

    @Test
    @DisplayName("프로젝트의 모든 CORS Origin을 삭제할 수 있다")
    void deleteAllCorsOriginsByProjectId() {
        // given
        Long projectId = 1L;

        // when
        projectCorsOriginService.deleteAllCorsOriginsByProjectId(projectId);

        // then
        verify(projectCorsOriginRepository).deleteByProjectId(projectId);
    }
}
