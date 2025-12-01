package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 도메인 서버 수정 성공 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainServerUpdateResponse {

    private Long id;
    private String name;
    private ServerStatus status;
    private String healthCheckUrl;
    private String healthCheckInterval;
    private Boolean isHealthCheckActive;
    private Instant updatedAt;

    public static DomainServerUpdateResponse from(DomainServer domainServer) {
        return DomainServerUpdateResponse.builder()
            .id(domainServer.getId())
            .name(domainServer.getName())
            .status(domainServer.getStatus())
            .healthCheckUrl(domainServer.getHealthCheckUrl())
            .healthCheckInterval(domainServer.getHealthCheckInterval())
            .isHealthCheckActive(domainServer.getIsHealthCheckActive())
            .updatedAt(domainServer.getUpdatedAt())
            .build();
    }
}
