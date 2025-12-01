package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 도메인 서버 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainServerResponse {

    private Long id;
    private String name;
    private ServerStatus status;
    private String healthCheckUrl;
    private String healthCheckInterval;
    private Boolean isHealthCheckActive;
    private Instant lastCheckedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static DomainServerResponse from(DomainServer server) {
        return DomainServerResponse.builder()
            .id(server.getId())
            .name(server.getName())
            .status(server.getStatus())
            .healthCheckUrl(server.getHealthCheckUrl())
            .healthCheckInterval(server.getHealthCheckInterval())
            .isHealthCheckActive(server.getIsHealthCheckActive())
            .lastCheckedAt(server.getLastCheckedAt())
            .createdAt(server.getCreatedAt())
            .updatedAt(server.getUpdatedAt())
            .build();
    }
}
