package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Mock API 핵심 정보 Base DTO
 * 다른 MockApi Response DTO들이 이 DTO를 임베드하여 사용
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiCoreResponse {

    private Long id;
    private String name;
    private HttpMethod httpMethod;
    private String endpointPath;
    private Integer statusCode;
    private Boolean isActive;

    public static MockApiCoreResponse from(MockApi mockApi) {
        return MockApiCoreResponse.builder()
            .id(mockApi.getId())
            .name(mockApi.getName())
            .httpMethod(mockApi.getHttpMethod())
            .endpointPath(mockApi.getEndpointPath())
            .statusCode(mockApi.getStatusCode())
            .isActive(mockApi.getIsActive())
            .build();
    }
}
