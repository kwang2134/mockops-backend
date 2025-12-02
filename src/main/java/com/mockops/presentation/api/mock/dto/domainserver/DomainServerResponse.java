package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;

import java.time.Instant;

/**
 * 도메인 서버 응답 DTO
 */
public record DomainServerResponse(
        Long id,
        String name,
        ServerStatus status,
        String healthCheckUrl,
        String healthCheckInterval,
        Boolean isHealthCheckActive,
        Instant lastCheckedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static DomainServerResponse from(DomainServer server) {
        return new DomainServerResponse(
                server.getId(),
                server.getName(),
                server.getStatus(),
                server.getHealthCheckUrl(),
                server.getHealthCheckInterval(),
                server.getIsHealthCheckActive(),
                server.getLastCheckedAt(),
                server.getCreatedAt(),
                server.getUpdatedAt()
        );
    }
}
