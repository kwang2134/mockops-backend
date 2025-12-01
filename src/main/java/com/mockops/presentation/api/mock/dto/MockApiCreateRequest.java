package com.mockops.presentation.api.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mock API 생성 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MockApiCreateRequest {

    @Size(min = 1, max = 100, message = "Mock API 이름은 1자 이상 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "HTTP 메서드는 필수입니다.")
    private HttpMethod httpMethod;

    @NotBlank(message = "엔드포인트 경로는 필수입니다.")
    @Pattern(regexp = "^/.*", message = "엔드포인트 경로는 /로 시작해야 합니다.")
    @Size(max = 255, message = "엔드포인트 경로는 255자 이하여야 합니다.")
    private String endpointPath;

    @NotBlank(message = "응답 본문은 필수입니다.")
    private String responseBody;

    @Min(value = 100, message = "상태 코드는 100 이상이어야 합니다.")
    @Max(value = 599, message = "상태 코드는 599 이하여야 합니다.")
    private Integer statusCode;

    private Boolean isActive;
}
