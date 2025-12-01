package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Mock API 수정 성공 응답 DTO
 * MockApiCoreResponse 임베드 + responseBody + updatedAt
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiUpdateResponse {

    // MockApiCoreResponse 임베드
    private Long id;
    private String name;
    private HttpMethod httpMethod;
    private String endpointPath;
    private Integer statusCode;
    private Boolean isActive;

    // 추가 필드
    private String responseBody;
    private Instant updatedAt;

    public static MockApiUpdateResponse from(MockApi mockApi) {
        return MockApiUpdateResponse.builder()
            .id(mockApi.getId())
            .name(mockApi.getName())
            .httpMethod(mockApi.getHttpMethod())
            .endpointPath(mockApi.getEndpointPath())
            .statusCode(mockApi.getStatusCode())
            .isActive(mockApi.getIsActive())
            .responseBody(mockApi.getResponseBody())
            .updatedAt(mockApi.getUpdatedAt())
            .build();
    }
}
