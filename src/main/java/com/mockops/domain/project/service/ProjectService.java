package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.domain.project.repository.ProjectRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.project.dto.ProjectCreateResponse;
import com.mockops.presentation.api.project.dto.ProjectDetailResponse;
import com.mockops.presentation.api.project.dto.ProjectPageResponse;
import com.mockops.presentation.api.project.dto.ProjectResponse;
import com.mockops.presentation.api.project.dto.ProjectUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> ErrorCode.PROJECT_NOT_FOUND.domainException(
                        "해당하는 프로젝트가 존재하지 않습니다. projectId=" + projectId
                ));
    }

    public Project getProjectByOwnerIdAndName(Long ownerId, String name) {
        return projectRepository.findByOwnerIdAndName(ownerId, name)
                .orElseThrow(() -> ErrorCode.PROJECT_NOT_FOUND.domainException(
                        "해당하는 프로젝트가 존재하지 않습니다. ownerId=" + ownerId + ", name=" + name
                ));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * 프로젝트 ID 목록으로 페이징 조회
     */
    public Page<Project> getProjectsByIds(List<Long> projectIds, Pageable pageable) {
        return projectRepository.findByIdIn(projectIds, pageable);
    }

    /**
     * 내 프로젝트 목록 조회 (페이지 기반) - 비즈니스 로직 포함
     */
    public ProjectPageResponse getMyProjects(Long userId, Pageable pageable) {
        // 사용자가 속한 프로젝트 멤버 목록 조회
        List<ProjectMember> myMemberships = projectMemberRepository.findByUserId(userId);

        // 프로젝트 ID 목록 추출
        List<Long> projectIds = myMemberships.stream()
                .map(ProjectMember::getProjectId)
                .toList();

        // 데이터베이스 레벨에서 페이징 처리
        Page<Project> projectPage = projectRepository.findByIdIn(projectIds, pageable);

        // DTO 변환
        List<ProjectResponse> projectResponses = projectPage.getContent().stream()
                .map(project -> {
                    User owner = userService.getUserById(project.getOwnerId());
                    return ProjectResponse.from(project, owner.getNickname());
                })
                .collect(Collectors.toList());

        return new ProjectPageResponse(
                projectResponses,
                projectPage.getTotalPages(),
                projectPage.getTotalElements(),
                pageable.getPageNumber()
        );
    }

    /**
     * 프로젝트 상세 조회 - Response DTO 반환
     * 권한: PROJECT_MEMBER 이상
     */
    public ProjectDetailResponse getProjectDetails(Long projectId, Long currentUserId) {
        // 권한 검증: 프로젝트 멤버 여부 확인
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        Project project = getProjectById(projectId);
        User owner = userService.getUserById(project.getOwnerId());
        return ProjectDetailResponse.from(project, owner);
    }

    /**
     * 프로젝트 정보 수정 - ProjectUpdateResponse 반환
     * 권한: PROJECT_OWNER
     */
    @Transactional
    public ProjectUpdateResponse updateProjectWithDetails(Long projectId, Long currentUserId, String description, String slackWebhookUrl) {
        // 권한 검증: 프로젝트 소유자만 수정 가능
        projectMemberService.validateOwner(projectId, currentUserId);

        Project project = updateProjectInfo(projectId, description, slackWebhookUrl);
        return ProjectUpdateResponse.from(project);
    }

    /**
     * 프로젝트 생성 - ProjectCreateResponse 반환
     * 생성자를 자동으로 OWNER로 추가
     */
    @Transactional
    public ProjectCreateResponse createProject(String name, String description, Long ownerId, String slackWebhookUrl) {
        if (projectRepository.existsByOwnerIdAndName(ownerId, name)) {
            throw ErrorCode.PROJECT_NAME_DUPLICATED.domainException(
                    "이미 존재하는 프로젝트 이름입니다. ownerId=" + ownerId + ", name=" + name
            );
        }

        Project project = Project.builder()
                .name(name)
                .description(description)
                .ownerId(ownerId)
                .slackWebhookUrl(slackWebhookUrl)
                .build();

        Project savedProject = projectRepository.save(project);

        // 생성자를 OWNER로 자동 추가
        projectMemberService.addProjectMember(savedProject.getId(), ownerId, MemberRole.OWNER);

        return ProjectCreateResponse.from(savedProject);
    }

    @Transactional
    public Project updateProjectInfo(Long projectId, String description, String slackWebhookUrl) {
        Project project = getProjectById(projectId);
        project.updateProjectInfo(description, slackWebhookUrl);
        return project;
    }

    /**
     * 프로젝트 삭제
     * 권한: PROJECT_OWNER
     */
    @Transactional
    public void deleteProject(Long projectId, Long currentUserId) {
        // 권한 검증: 프로젝트 소유자만 삭제 가능
        projectMemberService.validateOwner(projectId, currentUserId);

        Project project = getProjectById(projectId);
        projectRepository.delete(project);
    }
}
