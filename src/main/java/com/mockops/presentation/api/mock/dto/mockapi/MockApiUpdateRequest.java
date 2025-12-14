package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Mock API 수정 요청 DTO
 */
public record MockApiUpdateRequest(
        @Size(min = 1, max = 100, message = "Mock API 이름은 1자 이상 100자 이하여야 합니다.")
        String name,

        HttpMethod httpMethod,

        @Pattern(regexp = "^/.*", message = "엔드포인트 경로는 /로 시작해야 합니다.")
        @Size(max = 255, message = "엔드포인트 경로는 255자 이하여야 합니다.")
        String endpointPath,

        String responseBody,

        @Min(value = 100, message = "상태 코드는 100 이상이어야 합니다.")
        @Max(value = 599, message = "상태 코드는 599 이하여야 합니다.")
        Integer statusCode,

        Boolean isActive
) {
}
