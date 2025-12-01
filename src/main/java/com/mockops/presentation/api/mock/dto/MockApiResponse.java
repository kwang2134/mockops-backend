package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Mock API 목록 응답 DTO (responseBody 제외)
 * MockApiCoreResponse 임베드 + updatedAt
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiResponse {

    // MockApiCoreResponse 임베드
    private Long id;
    private String name;
    private HttpMethod httpMethod;
    private String endpointPath;
    private Integer statusCode;
    private Boolean isActive;

    // 추가 필드
    private Instant updatedAt;

    public static MockApiResponse from(MockApi mockApi) {
        return MockApiResponse.builder()
            .id(mockApi.getId())
            .name(mockApi.getName())
            .httpMethod(mockApi.getHttpMethod())
            .endpointPath(mockApi.getEndpointPath())
            .statusCode(mockApi.getStatusCode())
            .isActive(mockApi.getIsActive())
            .updatedAt(mockApi.getUpdatedAt())
            .build();
    }
}
