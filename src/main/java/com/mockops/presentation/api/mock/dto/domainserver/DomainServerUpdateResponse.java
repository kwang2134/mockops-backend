package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;

import java.time.Instant;

/**
 * 도메인 서버 수정 성공 응답 DTO
 */
public record DomainServerUpdateResponse(
        Long id,
        String name,
        String slug,
        ServerStatus status,
        String healthCheckUrl,
        String healthCheckInterval,
        Boolean isHealthCheckActive,
        Instant updatedAt
) {
    public static DomainServerUpdateResponse from(DomainServer domainServer) {
        return new DomainServerUpdateResponse(
                domainServer.getId(),
                domainServer.getName(),
                domainServer.getSlug(),
                domainServer.getStatus(),
                domainServer.getHealthCheckUrl(),
                domainServer.getHealthCheckInterval(),
                domainServer.getIsHealthCheckActive(),
                domainServer.getUpdatedAt()
        );
    }
}
