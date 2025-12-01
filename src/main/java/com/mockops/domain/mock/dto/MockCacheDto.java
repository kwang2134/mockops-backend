package com.mockops.domain.mock.dto;

import com.mockops.domain.mock.entity.MockApi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis에 캐싱할 Mock API 데이터 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockCacheDto implements Serializable {

    private Long mockId;
    private Integer statusCode;
    private String responseBody;

    public static MockCacheDto from(MockApi mockApi) {
        return MockCacheDto.builder()
            .mockId(mockApi.getId())
            .statusCode(mockApi.getStatusCode())
            .responseBody(mockApi.getResponseBody())
            .build();
    }
}
