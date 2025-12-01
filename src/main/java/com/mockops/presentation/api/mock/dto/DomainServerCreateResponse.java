package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.DomainServer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 도메인 서버 생성 성공 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainServerCreateResponse {

    private Long id;
    private String name;
    private Instant createdAt;

    public static DomainServerCreateResponse from(DomainServer domainServer) {
        return DomainServerCreateResponse.builder()
            .id(domainServer.getId())
            .name(domainServer.getName())
            .createdAt(domainServer.getCreatedAt())
            .build();
    }
}
