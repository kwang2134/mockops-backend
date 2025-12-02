package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;

/**
 * Mock API 핵심 정보 Base DTO
 * 다른 MockApi Response DTO들이 이 DTO를 임베드하여 사용
 */
public record MockApiCoreResponse(
        Long id,
        String name,
        HttpMethod httpMethod,
        String endpointPath,
        Integer statusCode,
        Boolean isActive
) {
    public static MockApiCoreResponse from(MockApi mockApi) {
        return new MockApiCoreResponse(
                mockApi.getId(),
                mockApi.getName(),
                mockApi.getHttpMethod(),
                mockApi.getEndpointPath(),
                mockApi.getStatusCode(),
                mockApi.getIsActive()
        );
    }
}
