package com.mockops.domain.webhook.service;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.service.DomainServerService;
import com.mockops.domain.webhook.port.DeployHealthCheckJobPort;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.webhook.dto.DeploymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배포 이벤트 비동기 처리 서비스
 * CI/CD 배포 완료 Webhook 이벤트를 처리하여 DomainServer 상태를 업데이트하고 헬스 체크를 등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentService {

    private final DomainServerService domainServerService;
    private final DeployHealthCheckJobPort deployHealthCheckJobPort;

    /**
     * Webhook Deploy 이벤트 발생 시 Redis 작업 등록
     * @param projectId
     * @param request
     */
    @Transactional
    public void registerDeploymentJob(Long projectId, DeploymentRequest request) {
        log.info("배포 이벤트 healthCheck job 등록 시작: projectId={}, projectName={}, domainServerName={}",
                projectId, request.projectName(), request.domainServerName());

        DomainServer domainServer = domainServerService.findServerByProjectIdAndName(projectId, request.domainServerName())
                .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException("도메인 서버가 존재하지 않습니다. serverName=" + request.domainServerName()));

        deployHealthCheckJobPort.registerDeployHealthCheck(domainServer.getId(), request.healthCheckUrl());
        log.info("배포 이벤트 Job Redis 등록 완료");
    }
}