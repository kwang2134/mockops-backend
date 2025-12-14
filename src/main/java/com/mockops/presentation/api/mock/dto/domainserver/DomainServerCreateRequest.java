package com.mockops.presentation.api.mock.dto.domainserver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 도메인 서버 생성 요청 DTO
 */
public record DomainServerCreateRequest(
        @NotBlank(message = "서버 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "서버 이름은 1자 이상 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "서버 slug는 필수입니다.")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "slug는 영문 소문자, 숫자, 하이픈(-)만 사용 가능합니다.")
        @Size(min = 1, max = 100, message = "slug는 1자 이상 100자 이하여야 합니다.")
        String slug,

        @Size(max = 255, message = "헬스체크 URL은 255자 이하여야 합니다.")
        String healthCheckUrl,

        @Pattern(regexp = "^\\d+[smh]$", message = "헬스체크 주기는 숫자 + 단위(s/m/h) 형식이어야 합니다. 예: 10m, 30s, 1h")
        String healthCheckInterval
) {
}
