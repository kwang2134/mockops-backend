package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;

import java.time.Instant;

/**
 * Mock API 생성 성공 응답 DTO
 * MockApiCoreResponse 임베드 + responseBody + createdAt
 */
public record MockApiCreateResponse(
        Long id,
        String name,
        HttpMethod httpMethod,
        String endpointPath,
        Integer statusCode,
        Boolean isActive,
        String responseBody,
        Instant createdAt
) {
    public static MockApiCreateResponse from(MockApi mockApi) {
        return new MockApiCreateResponse(
                mockApi.getId(),
                mockApi.getName(),
                mockApi.getHttpMethod(),
                mockApi.getEndpointPath(),
                mockApi.getStatusCode(),
                mockApi.getIsActive(),
                mockApi.getResponseBody(),
                mockApi.getCreatedAt()
        );
    }
}
