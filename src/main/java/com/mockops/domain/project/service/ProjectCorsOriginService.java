package com.mockops.domain.project.service;

import com.mockops.domain.project.entity.ProjectCorsOrigin;
import com.mockops.domain.project.infrastructure.CorsOriginCachePort;
import com.mockops.domain.project.repository.ProjectCorsOriginRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.project.dto.projectcorsorigin.CorsOriginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectCorsOriginService {

    private final ProjectCorsOriginRepository projectCorsOriginRepository;
    private final ProjectMemberService projectMemberService;
    private final CorsOriginCachePort corsOriginCachePort;

    public ProjectCorsOrigin getCorsOriginById(Long corsOriginId) {
        return projectCorsOriginRepository.findById(corsOriginId)
                .orElseThrow(() -> ErrorCode.PROJECT_CORS_ORIGIN_NOT_FOUND.domainException(
                        "해당하는 CORS Origin이 존재하지 않습니다. corsOriginId=" + corsOriginId
                ));
    }

    public List<ProjectCorsOrigin> getCorsOriginsByProjectId(Long projectId) {
        return projectCorsOriginRepository.findByProjectId(projectId);
    }

    /**
     * CORS Origin 목록 조회 - Response DTO 반환
     * 권한: PROJECT_MEMBER 이상
     */
    public List<CorsOriginResponse> getCorsOriginsWithResponse(Long projectId, Long currentUserId) {
        // 권한 검증: 프로젝트 멤버 여부 확인
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        List<ProjectCorsOrigin> origins = projectCorsOriginRepository.findByProjectId(projectId);
        return origins.stream()
                .map(CorsOriginResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트의 허용된 Origin 목록을 캐시에서 조회 (캐시 미스 시 DB 조회 후 캐싱)
     */
    public Set<String> getAllowedOriginsWithCache(Long projectId) {
        // 1. 캐시 조회
        Set<String> cachedOrigins = corsOriginCachePort.getAllowedOrigins(projectId);

        // 2. 캐시 미스 시 DB 조회 후 캐싱
        if (cachedOrigins.isEmpty()) {
            List<ProjectCorsOrigin> origins = projectCorsOriginRepository.findByProjectId(projectId);
            cachedOrigins = origins.stream()
                    .map(ProjectCorsOrigin::getOriginUrl)
                    .collect(Collectors.toSet());

            if (!cachedOrigins.isEmpty()) {
                corsOriginCachePort.cacheAllowedOrigins(projectId, cachedOrigins);
            }
        }

        return cachedOrigins;
    }

    /**
     * Origin이 프로젝트에서 허용되는지 확인 (캐시 우선)
     */
    public boolean isOriginAllowed(Long projectId, String origin) {
        // 캐시에 없으면 DB 조회 후 캐싱
        Set<String> allowedOrigins = getAllowedOriginsWithCache(projectId);
        return allowedOrigins.contains(origin);
    }

    /**
     * CORS Origin 추가 - Response DTO 반환
     * 권한: PROJECT_DEVELOPER 이상
     */
    @Transactional
    public CorsOriginResponse addCorsOriginWithResponse(Long projectId, Long currentUserId, String originUrl) {
        // 권한 검증: DEVELOPER 이상만 CORS Origin 추가 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.DEVELOPER);

        ProjectCorsOrigin corsOrigin = addCorsOrigin(projectId, originUrl);
        return CorsOriginResponse.from(corsOrigin);
    }

    @Transactional
    public ProjectCorsOrigin addCorsOrigin(Long projectId, String originUrl) {
        if (projectCorsOriginRepository.existsByProjectIdAndOriginUrl(projectId, originUrl)) {
            throw ErrorCode.PROJECT_CORS_ORIGIN_DUPLICATED.domainException(
                    "이미 등록된 CORS Origin입니다. projectId=" + projectId + ", originUrl=" + originUrl
            );
        }

        ProjectCorsOrigin corsOrigin = ProjectCorsOrigin.builder()
                .projectId(projectId)
                .originUrl(originUrl)
                .build();

        ProjectCorsOrigin saved = projectCorsOriginRepository.save(corsOrigin);

        // 캐시 갱신
        refreshCache(projectId);

        return saved;
    }

    /**
     * CORS Origin 삭제
     * 권한: PROJECT_DEVELOPER 이상
     */
    @Transactional
    public void deleteCorsOrigin(Long corsOriginId, Long currentUserId) {
        ProjectCorsOrigin corsOrigin = getCorsOriginById(corsOriginId);
        Long projectId = corsOrigin.getProjectId();

        // 권한 검증: DEVELOPER 이상만 CORS Origin 삭제 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.DEVELOPER);

        projectCorsOriginRepository.delete(corsOrigin);

        // 캐시 갱신
        refreshCache(projectId);
    }

    @Transactional
    public void deleteAllCorsOriginsByProjectId(Long projectId) {
        projectCorsOriginRepository.deleteByProjectId(projectId);

        // 캐시 무효화
        corsOriginCachePort.evictCache(projectId);
    }

    /**
     * 프로젝트의 CORS Origin 캐시를 갱신
     */
    private void refreshCache(Long projectId) {
        List<ProjectCorsOrigin> origins = projectCorsOriginRepository.findByProjectId(projectId);
        Set<String> originUrls = origins.stream()
                .map(ProjectCorsOrigin::getOriginUrl)
                .collect(Collectors.toSet());

        corsOriginCachePort.cacheAllowedOrigins(projectId, originUrls);
    }
}
