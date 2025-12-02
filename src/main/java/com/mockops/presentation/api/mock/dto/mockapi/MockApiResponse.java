package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;

import java.time.Instant;

/**
 * Mock API 목록 응답 DTO (responseBody 제외)
 * MockApiCoreResponse 임베드 + updatedAt
 */
public record MockApiResponse(
        // MockApiCoreResponse 임베드
        Long id,
        String name,
        HttpMethod httpMethod,
        String endpointPath,
        Integer statusCode,
        Boolean isActive,

        // 추가 필드
        Instant updatedAt
) {
    public static MockApiResponse from(MockApi mockApi) {
        return new MockApiResponse(
                mockApi.getId(),
                mockApi.getName(),
                mockApi.getHttpMethod(),
                mockApi.getEndpointPath(),
                mockApi.getStatusCode(),
                mockApi.getIsActive(),
                mockApi.getUpdatedAt()
        );
    }
}
