package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.repository.ProjectRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 테스트")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private com.mockops.domain.project.repository.ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private com.mockops.domain.user.service.UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("ID로 프로젝트를 조회할 수 있다")
    void getProjectById() {
        // given
        Long projectId = 1L;
        Project project = Project.builder()
                .name("TestProject")
                .description("Test Description")
                .ownerId(1L)
                .build();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

        // when
        Project foundProject = projectService.getProjectById(projectId);

        // then
        assertThat(foundProject).isNotNull();
        assertThat(foundProject.getName()).isEqualTo("TestProject");
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getProjectByIdNotFound() {
        // given
        given(projectRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProjectById(1L))
                .isInstanceOf(BusinessException.class);
        verify(projectRepository).findById(1L);
    }

    @Test
    @DisplayName("사용자 ID와 이름으로 프로젝트를 조회할 수 있다")
    void getProjectByOwnerIdAndName() {
        // given
        Long ownerId = 1L;
        String name = "TestProject";
        Project project = Project.builder()
                .name(name)
                .description("Test Description")
                .ownerId(ownerId)
                .build();

        given(projectRepository.findByOwnerIdAndName(ownerId, name)).willReturn(Optional.of(project));

        // when
        Project foundProject = projectService.getProjectByOwnerIdAndName(ownerId, name);

        // then
        assertThat(foundProject).isNotNull();
        assertThat(foundProject.getName()).isEqualTo(name);
        assertThat(foundProject.getOwnerId()).isEqualTo(ownerId);
        verify(projectRepository).findByOwnerIdAndName(ownerId, name);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 예외가 발생한다")
    void getProjectByOwnerIdAndNameNotFound() {
        // given
        given(projectRepository.findByOwnerIdAndName(anyLong(), anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProjectByOwnerIdAndName(1L, "NonExistent"))
                .isInstanceOf(BusinessException.class);
        verify(projectRepository).findByOwnerIdAndName(1L, "NonExistent");
    }

    @Test
    @DisplayName("모든 프로젝트를 조회할 수 있다")
    void getAllProjects() {
        // given
        List<Project> projects = List.of(
                Project.builder().name("Project1").ownerId(1L).build(),
                Project.builder().name("Project2").ownerId(1L).build()
        );
        given(projectRepository.findAll()).willReturn(projects);

        // when
        List<Project> foundProjects = projectService.getAllProjects();

        // then
        assertThat(foundProjects).hasSize(2);
        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("프로젝트를 생성할 수 있다")
    void createProject() {
        // given
        String name = "TestProject";
        String description = "Test Description";
        Long ownerId = 1L;
        String slackWebhookUrl = "https://hooks.slack.com/test";

        Project project = Project.builder()
                .name(name)
                .description(description)
                .ownerId(ownerId)
                .slackWebhookUrl(slackWebhookUrl)
                .build();

        given(projectRepository.existsByOwnerIdAndName(ownerId, name)).willReturn(false);
        given(projectRepository.save(any(Project.class))).willReturn(project);

        // when
        Project createdProject = projectService.createProject(name, description, ownerId, slackWebhookUrl);

        // then
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo(name);
        verify(projectRepository).existsByOwnerIdAndName(ownerId, name);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("같은 사용자가 중복된 이름으로 프로젝트 생성 시 예외가 발생한다")
    void createProjectWithDuplicateName() {
        // given
        String name = "TestProject";
        Long ownerId = 1L;
        given(projectRepository.existsByOwnerIdAndName(ownerId, name)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> projectService.createProject(name, "desc", ownerId, null))
                .isInstanceOf(BusinessException.class);
        verify(projectRepository).existsByOwnerIdAndName(ownerId, name);
    }

    @Test
    @DisplayName("프로젝트 정보를 수정할 수 있다")
    void updateProjectInfo() {
        // given
        Long projectId = 1L;
        String newDescription = "New Description";
        String newSlackWebhookUrl = "https://hooks.slack.com/new";

        Project project = Project.builder()
                .name("TestProject")
                .description("Old Description")
                .ownerId(1L)
                .slackWebhookUrl("https://hooks.slack.com/old")
                .build();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

        // when
        Project updatedProject = projectService.updateProjectInfo(projectId, newDescription, newSlackWebhookUrl);

        // then
        assertThat(updatedProject.getDescription()).isEqualTo(newDescription);
        assertThat(updatedProject.getSlackWebhookUrl()).isEqualTo(newSlackWebhookUrl);
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("프로젝트를 삭제할 수 있다")
    void deleteProject() {
        // given
        Long projectId = 1L;
        Long userId = 1L;
        Project project = Project.builder()
                .name("TestProject")
                .ownerId(1L)
                .build();

        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));

        // when
        projectService.deleteProject(projectId, userId);

        // then
        verify(projectRepository).findById(projectId);
        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 삭제 시 예외가 발생한다")
    void deleteProjectNotFound() {
        // given
        given(projectRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject(1L, 1L))
                .isInstanceOf(BusinessException.class);
        verify(projectRepository).findById(1L);
    }
}
