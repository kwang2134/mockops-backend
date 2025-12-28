package com.mockops.domain.mock.service;

import com.mockops.domain.healthcheck.service.HealthCheckService;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.mock.repository.DomainServerRepository;
import com.mockops.domain.notification.repository.NotificationRepository;
import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.mock.dto.domainserver.*;
import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 도메인 서버 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainServerService {

    private final DomainServerRepository domainServerRepository;
    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;
    private final HealthCheckService healthCheckService;
    private final NotificationRepository notificationRepository;

    /**
     * 서버 ID로 조회
     */
    public DomainServer getDomainServerById(Long serverId) {
        return domainServerRepository.findById(serverId)
            .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException(
                "존재하지 않는 도메인 서버입니다. serverId=" + serverId
            ));
    }

    /**
     * 프로젝트 ID와 서버 이름으로 조회
     */
    public Optional<DomainServer> findServerByProjectIdAndName(Long projectId, String name) {
        return domainServerRepository.findByProjectIdAndName(projectId, name);
    }

    /**
     * 프로젝트 ID와 slug로 조회
     */
    public DomainServer getServerByProjectIdAndSlug(Long projectId, String slug) {
        return domainServerRepository.findByProjectIdAndSlug(projectId, slug)
            .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException(
                "존재하지 않는 도메인 서버입니다. projectId=" + projectId + ", slug=" + slug
            ));
    }

    /**
     * 프로젝트의 서버 목록 조회 (페이징) - DomainServerSimpleResponse 반환 (미확인 알림 개수 포함)
     */
    public Page<DomainServerSimpleResponse> getServersByProjectWithResponse(Long projectId, Long currentUserId, Pageable pageable) {
        // 권한 검증: 프로젝트 멤버만 조회 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        Page<DomainServer> servers = domainServerRepository.findByProjectId(projectId, pageable);

        // 각 서버별로 미확인 알림 개수를 조회하여 DomainServerSimpleResponse로 변환
        return servers.map(server -> {
            Integer unreadCount = notificationRepository.countUnreadByDomainServerId(server.getId());
            return DomainServerSimpleResponse.from(server, unreadCount);
        });
    }

    /**
     * 프로젝트의 서버 목록 조회 (페이징) - 내부용
     */
    public Page<DomainServer> getServersByProject(Long projectId, Long currentUserId, Pageable pageable) {
        // 권한 검증: 프로젝트 멤버만 조회 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        return domainServerRepository.findByProjectId(projectId, pageable);
    }

    /**
     * 도메인 서버 검색 (이름, 상태)
     * 권한: 프로젝트 멤버(VIEWER) 이상
     */
    public Page<DomainServerSimpleResponse> searchDomainServers(Long projectId, String name, ServerStatus status, Long currentUserId, Pageable pageable) {
        // 권한 검증: 프로젝트 멤버만 조회 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.VIEWER);

        // QueryDSL을 사용한 동적 검색
        Page<DomainServer> servers = domainServerRepository.searchDomainServers(projectId, name, status, pageable);

        // 각 서버별로 미확인 알림 개수를 조회하여 DomainServerSimpleResponse로 변환
        return servers.map(server -> {
            Integer unreadCount = notificationRepository.countUnreadByDomainServerId(server.getId());
            return DomainServerSimpleResponse.from(server, unreadCount);
        });
    }

    /**
     * 서버 상세 조회 - DTO 반환
     */
    public DomainServerResponse getServerWithResponse(Long serverId, Long currentUserId) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: 프로젝트 멤버만 조회 가능
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.VIEWER);

        return DomainServerResponse.from(server);
    }

    /**
     * 서버 생성 (MOCKING 상태로) - DomainServerCreateResponse 반환
     */
    @Transactional
    public DomainServerCreateResponse createServerWithResponse(Long projectId, String name, String slug,
                                                         String healthCheckUrl, String healthCheckInterval, Long currentUserId) {
        DomainServer server = createServer(projectId, name, slug, healthCheckUrl, healthCheckInterval, currentUserId);
        return DomainServerCreateResponse.from(server);
    }

    /**
     * 서버 생성 (MOCKING 상태로) - 내부용
     */
    @Transactional
    public DomainServer createServer(Long projectId, String name, String slug, String healthCheckUrl,
                                    String healthCheckInterval, Long currentUserId) {
        // 권한 검증: DEVELOPER 이상만 생성 가능
        projectMemberService.validateMemberPermission(projectId, currentUserId, MemberRole.DEVELOPER);

        // slug 중복 확인 (projectId와 slug의 복합 유니크 제약)
        if (domainServerRepository.existsByProjectIdAndSlug(projectId, slug)) {
            throw ErrorCode.DOMAIN_SERVER_DUPLICATED.serviceException(
                "이미 존재하는 서버 slug입니다. projectId=" + projectId + ", slug=" + slug
            );
        }

        DomainServer server = DomainServer.builder()
            .projectId(projectId)
            .name(name)
            .slug(slug)
            .status(ServerStatus.MOCKING)
            .healthCheckUrl(healthCheckUrl)
            .healthCheckInterval(healthCheckInterval)
            .build();

        return domainServerRepository.save(server);
    }

    /**
     * 서버 정보 수정 - DomainServerUpdateResponse 반환
     */
    @Transactional
    public DomainServerUpdateResponse updateServerWithResponse(Long serverId, String name, String healthCheckUrl,
                                                         String healthCheckInterval, ServerStatus status,
                                                         Boolean isHealthCheckActive, Long currentUserId) {
        DomainServer server = updateServer(serverId, name, healthCheckUrl, healthCheckInterval, status, isHealthCheckActive, currentUserId);
        return DomainServerUpdateResponse.from(server);
    }

    /**
     * 서버 정보 수정 - 내부용
     */
    @Transactional
    public DomainServer updateServer(Long serverId, String name, String healthCheckUrl,
                                    String healthCheckInterval, ServerStatus status,
                                    Boolean isHealthCheckActive, Long currentUserId) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: DEVELOPER 이상만 수정 가능
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 기존 헬스 체크 활성화 상태 저장
        Boolean previousIsHealthCheckActive = server.getIsHealthCheckActive();

        // 서버 정보 업데이트
        server.updateServerInfo(name, healthCheckUrl, healthCheckInterval, status, isHealthCheckActive);

        // 헬스 체크 활성화 플래그 변경 감지 및 Redis 동기화
        Boolean currentIsHealthCheckActive = server.getIsHealthCheckActive();

        if (!previousIsHealthCheckActive.equals(currentIsHealthCheckActive)) {
            if (currentIsHealthCheckActive) {
                // false -> true: Redis에 헬스 체크 작업 등록
                healthCheckService.registerHealthCheck(server);
                log.info("헬스 체크 활성화: serverId={}, interval={}", serverId, server.getHealthCheckInterval());
            } else {
                // true -> false: Redis에서 헬스 체크 작업 제거
                healthCheckService.unregisterHealthCheck(server);
                log.info("헬스 체크 비활성화: serverId={}", serverId);
            }
        }

        return server;
    }

    /**
     * 서버 삭제
     */
    @Transactional
    public void deleteServer(Long serverId, Long currentUserId) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: DEVELOPER 이상만 삭제 가능
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // Redis에서 헬스 체크 작업 제거
        if (server.getIsHealthCheckActive()) {
            healthCheckService.unregisterHealthCheck(server);
            log.info("서버 삭제로 인한 헬스 체크 제거: serverId={}", serverId);
        }

        domainServerRepository.delete(server);

        log.info("도메인 서버 삭제 완료: serverId={}, projectId={}", serverId, server.getProjectId());
    }

    /**
     * 서버 상태 변경
     */
    @Transactional
    public void updateServerStatus(Long serverId, ServerStatus newStatus) {
        DomainServer server = getDomainServerById(serverId);
        server.updateStatus(newStatus);
        server.updateLastCheckedAt();
    }

    /**
     * 도메인 서버 담당 멤버 목록 조회 (Offset 기반 페이징)
     * 권한: 프로젝트 멤버(VIEWER) 이상
     */
    public DomainServerMemberListResponse getDomainServerMembers(Long serverId, Long currentUserId, Integer offset, int size) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: 프로젝트 멤버만 조회 가능
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.VIEWER);

        // 로그인 사용자의 멤버 정보 조회 (담당 도메인 서버 ID 확인)
        ProjectMember currentMember = projectMemberService.getProjectMemberByProjectIdAndUserId(server.getProjectId(), currentUserId);
        Long myDomainServerId = currentMember.getDomainServerId();

        // offset이 null이면 0으로 처리
        int actualOffset = (offset == null) ? 0 : offset;

        // 데이터베이스 레벨에서 offset 기반 페이징 처리
        Pageable pageable = PageRequest.of(actualOffset / size, size + 1);
        List<ProjectMember> members = projectMemberRepository.findByDomainServerIdOrderByMemberRoleAscIdAsc(serverId, pageable);

        // hasNext 계산
        boolean hasNext = members.size() > size;
        List<ProjectMember> pagedMembers = hasNext
                ? members.subList(0, size)
                : members;

        // DTO 변환
        List<ProjectMemberResponse> memberResponses = pagedMembers.stream()
                .map(member -> {
                    User user = userService.getUserById(member.getUserId());
                    return ProjectMemberResponse.from(member, user.getNickname());
                })
                .collect(Collectors.toList());

        // nextOffset 계산
        Integer nextOffset = hasNext ? actualOffset + size : null;

        return DomainServerMemberListResponse.of(memberResponses, hasNext, nextOffset, myDomainServerId, serverId);
    }

    /**
     * 도메인 서버 참여
     * 권한: DEVELOPER 이상
     */
    @Transactional
    public void joinDomainServerAsMember(Long serverId, Long currentUserId) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: DEVELOPER 이상만 참여 가능
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 로그인 사용자의 멤버 정보 조회
        ProjectMember member = projectMemberService.getProjectMemberByProjectIdAndUserId(server.getProjectId(), currentUserId);

        // 이미 다른 도메인 서버에 참여 중인지 확인
        if (member.getDomainServerId() != null) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                    "이미 다른 도메인 서버에 참여 중입니다. currentServerId=" + member.getDomainServerId()
            );
        }

        // 도메인 서버 참여 (더티 체킹으로 자동 UPDATE)
        member.updateDomainServer(serverId);

        log.info("도메인 서버 참여 완료: serverId={}, userId={}, memberId={}", serverId, currentUserId, member.getId());
    }

    /**
     * 도메인 서버 나가기
     * 권한: DEVELOPER 이상
     */
    @Transactional
    public void leaveDomainServerAsMember(Long serverId, Long currentUserId) {
        DomainServer server = getDomainServerById(serverId);

        // 권한 검증: DEVELOPER 이상만 나갈 수 있음
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 로그인 사용자의 멤버 정보 조회
        ProjectMember member = projectMemberService.getProjectMemberByProjectIdAndUserId(server.getProjectId(), currentUserId);

        // 해당 도메인 서버를 담당하고 있는지 확인
        if (member.getDomainServerId() == null || !member.getDomainServerId().equals(serverId)) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                    "해당 도메인 서버에 참여하고 있지 않습니다. serverId=" + serverId
            );
        }

        // 도메인 서버 나가기 (더티 체킹으로 자동 UPDATE)
        member.clearDomainServer();

        log.info("도메인 서버 나가기 완료: serverId={}, userId={}, memberId={}", serverId, currentUserId, member.getId());
    }
}
