package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 도메인 서버 목록 개별 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainServerSimpleResponse {

    private Long id;
    private String name;
    private ServerStatus status;
    private Instant updatedAt;

    public static DomainServerSimpleResponse from(DomainServer domainServer) {
        return DomainServerSimpleResponse.builder()
            .id(domainServer.getId())
            .name(domainServer.getName())
            .status(domainServer.getStatus())
            .updatedAt(domainServer.getUpdatedAt())
            .build();
    }
}
