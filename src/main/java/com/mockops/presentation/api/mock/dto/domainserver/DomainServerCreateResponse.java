package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.domain.mock.entity.DomainServer;

import java.time.Instant;

/**
 * 도메인 서버 생성 성공 응답 DTO
 */
public record DomainServerCreateResponse(
        Long id,
        String name,
        String slug,
        Instant createdAt
) {
    public static DomainServerCreateResponse from(DomainServer domainServer) {
        return new DomainServerCreateResponse(
                domainServer.getId(),
                domainServer.getName(),
                domainServer.getSlug(),
                domainServer.getCreatedAt()
        );
    }
}
