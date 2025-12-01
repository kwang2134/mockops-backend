package com.mockops.domain.webhook.service;

import com.mockops.domain.healthcheck.service.HealthCheckService;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.mock.service.DomainServerService;
import com.mockops.presentation.api.webhook.dto.DeploymentEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 배포 이벤트 비동기 처리 서비스
 * CI/CD 배포 완료 Webhook 이벤트를 처리하여 DomainServer 상태를 업데이트하고 헬스 체크를 등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentEventService {

    private final DomainServerService domainServerService;
    private final HealthCheckService healthCheckService;

    /**
     * 배포 이벤트 비동기 처리
     * - DomainServer를 조회하거나 생성
     * - 상태를 DEPLOYED로 변경
     * - 헬스 체크 URL/주기 업데이트
     * - 헬스 체크 활성화 및 Redis 등록
     *
     * @param projectId 프로젝트 ID
     * @param request 배포 이벤트 정보
     */
    @Async
    @Transactional
    public void processDeploymentAsync(Long projectId, DeploymentEventRequest request) {
        log.info("배포 이벤트 비동기 처리 시작: projectId={}, projectName={}", projectId, request.projectName());

        try {
            // 1. 프로젝트 이름으로 DomainServer 조회
            Optional<DomainServer> serverOpt = domainServerService.findServerByProjectIdAndName(
                projectId, request.projectName()
            );

            DomainServer server;

            if (serverOpt.isPresent()) {
                // 2-A. 기존 서버가 있으면 업데이트
                server = serverOpt.get();
                log.info("기존 서버 발견: serverId={}, name={}", server.getId(), server.getName());

                // 기존 헬스 체크 비활성화 상태 저장
                boolean wasHealthCheckActive = server.getIsHealthCheckActive();

                // 서버 정보 업데이트 (상태를 DEPLOYED로, 헬스 체크 활성화)
                server.updateServerInfo(
                    null, // name은 변경하지 않음
                    request.healthCheckUrl(),
                    request.healthCheckInterval() != null ? request.healthCheckInterval() : "10m",
                    ServerStatus.DEPLOYED,
                    true // 헬스 체크 활성화
                );

                // 헬스 체크가 비활성화 상태였다면 Redis에 등록
                if (!wasHealthCheckActive) {
                    healthCheckService.registerHealthCheck(server);
                    log.info("헬스 체크 활성화 및 Redis 등록: serverId={}", server.getId());
                }

            } else {
                // 2-B. 서버가 존재하지 않으면 처리하지 않음 (Webhook은 상태 업데이트 전용)
                log.warn("배포 이벤트 처리 실패: 서버가 존재하지 않습니다. projectId={}, projectName={}",
                    projectId, request.projectName());
                log.warn("서버를 먼저 생성한 후 Webhook을 사용하세요.");
                return;
            }

            log.info("배포 이벤트 처리 완료: projectId={}, serverId={}, status={}",
                projectId, server.getId(), server.getStatus());

        } catch (Exception e) {
            log.error("배포 이벤트 처리 중 오류 발생: projectId={}, error={}",
                projectId, e.getMessage(), e);
            // 비동기 처리이므로 예외를 던지지 않고 로깅만 수행
        }
    }
}