package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;

import java.time.Instant;

/**
 * 도메인 서버 목록 개별 응답 DTO
 */
public record DomainServerSimpleResponse(
        Long id,
        String name,
        String slug,
        ServerStatus status,
        Instant updatedAt
) {
    public static DomainServerSimpleResponse from(DomainServer domainServer) {
        return new DomainServerSimpleResponse(
                domainServer.getId(),
                domainServer.getName(),
                domainServer.getSlug(),
                domainServer.getStatus(),
                domainServer.getUpdatedAt()
        );
    }
}
