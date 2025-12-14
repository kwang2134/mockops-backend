package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.domain.mock.entity.ServerStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 도메인 서버 수정 요청 DTO
 */
public record DomainServerUpdateRequest(
        @Size(min = 1, max = 50, message = "서버 이름은 1자 이상 50자 이하여야 합니다.")
        String name,

        @Size(max = 255, message = "헬스체크 URL은 255자 이하여야 합니다.")
        String healthCheckUrl,

        @Pattern(regexp = "^\\d+[smh]$", message = "헬스체크 주기는 숫자 + 단위(s/m/h) 형식이어야 합니다. 예: 10m, 30s, 1h")
        String healthCheckInterval,

        ServerStatus status,

        Boolean isHealthCheckActive
) {
}
