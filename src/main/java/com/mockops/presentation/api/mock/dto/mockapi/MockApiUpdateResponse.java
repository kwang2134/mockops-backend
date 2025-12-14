package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;

import java.time.Instant;

/**
 * Mock API 수정 성공 응답 DTO
 * MockApiCoreResponse 임베드 + responseBody + updatedAt
 */
public record MockApiUpdateResponse(
        // MockApiCoreResponse 임베드
        Long id,
        String name,
        HttpMethod httpMethod,
        String endpointPath,
        String fullEndpoint,
        Integer statusCode,
        Boolean isActive,

        // 추가 필드
        String responseBody,
        Instant updatedAt
) {
    public static MockApiUpdateResponse from(MockApi mockApi, Long projectId, String serverSlug) {
        String fullEndpoint = "/mock/" + projectId + "/" + serverSlug + mockApi.getEndpointPath();
        return new MockApiUpdateResponse(
                mockApi.getId(),
                mockApi.getName(),
                mockApi.getHttpMethod(),
                mockApi.getEndpointPath(),
                fullEndpoint,
                mockApi.getStatusCode(),
                mockApi.getIsActive(),
                mockApi.getResponseBody(),
                mockApi.getUpdatedAt()
        );
    }
}
