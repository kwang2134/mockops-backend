package com.mockops.presentation.api.mock.dto.mockapi;

import com.mockops.domain.mock.entity.HttpMethod;
import jakarta.validation.constraints.*;

/**
 * Mock API 생성 요청 DTO
 */
public record MockApiCreateRequest(
        @Size(min = 1, max = 100, message = "Mock API 이름은 1자 이상 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "HTTP 메서드는 필수입니다.")
        HttpMethod httpMethod,

        @NotBlank(message = "엔드포인트 경로는 필수입니다.")
        @Pattern(regexp = "^/.*", message = "엔드포인트 경로는 /로 시작해야 합니다.")
        @Size(max = 255, message = "엔드포인트 경로는 255자 이하여야 합니다.")
        String endpointPath,

        @NotBlank(message = "응답 본문은 필수입니다.")
        String responseBody,

        @Min(value = 100, message = "상태 코드는 100 이상이어야 합니다.")
        @Max(value = 599, message = "상태 코드는 599 이하여야 합니다.")
        Integer statusCode,

        Boolean isActive
) {
}
