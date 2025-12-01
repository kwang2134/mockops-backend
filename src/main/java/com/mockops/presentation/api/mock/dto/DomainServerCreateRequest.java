package com.mockops.presentation.api.mock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도메인 서버 생성 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DomainServerCreateRequest {

    @NotBlank(message = "서버 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "서버 이름은 1자 이상 50자 이하여야 합니다.")
    private String name;

    @Size(max = 255, message = "헬스체크 URL은 255자 이하여야 합니다.")
    private String healthCheckUrl;

    @Pattern(regexp = "^\\d+[smh]$", message = "헬스체크 주기는 숫자 + 단위(s/m/h) 형식이어야 합니다. 예: 10m, 30s, 1h")
    private String healthCheckInterval;
}
