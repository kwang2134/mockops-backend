package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Mock API 생성 성공 응답 DTO
 * MockApiCoreResponse 임베드 + responseBody + createdAt
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiCreateResponse {

    // MockApiCoreResponse 임베드
    private Long id;
    private String name;
    private HttpMethod httpMethod;
    private String endpointPath;
    private Integer statusCode;
    private Boolean isActive;

    // 추가 필드
    private String responseBody;
    private Instant createdAt;

    public static MockApiCreateResponse from(MockApi mockApi) {
        return MockApiCreateResponse.builder()
            .id(mockApi.getId())
            .name(mockApi.getName())
            .httpMethod(mockApi.getHttpMethod())
            .endpointPath(mockApi.getEndpointPath())
            .statusCode(mockApi.getStatusCode())
            .isActive(mockApi.getIsActive())
            .responseBody(mockApi.getResponseBody())
            .createdAt(mockApi.getCreatedAt())
            .build();
    }
}
