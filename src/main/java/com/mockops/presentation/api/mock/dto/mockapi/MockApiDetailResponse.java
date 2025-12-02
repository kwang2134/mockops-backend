package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;

import java.time.Instant;

/**
 * Mock API 상세 정보 응답 DTO
 * MockApiCoreResponse 임베드 + responseBody + createdAt + updatedAt
 */
public record MockApiDetailResponse(
        // MockApiCoreResponse 임베드
        Long id,
        String name,
        HttpMethod httpMethod,
        String endpointPath,
        Integer statusCode,
        Boolean isActive,

        // 추가 필드
        String responseBody,
        Instant createdAt,
        Instant updatedAt
) {
    public static MockApiDetailResponse from(MockApi mockApi) {
        return new MockApiDetailResponse(
                mockApi.getId(),
                mockApi.getName(),
                mockApi.getHttpMethod(),
                mockApi.getEndpointPath(),
                mockApi.getStatusCode(),
                mockApi.getIsActive(),
                mockApi.getResponseBody(),
                mockApi.getCreatedAt(),
                mockApi.getUpdatedAt()
        );
    }
}
