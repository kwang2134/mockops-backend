package com.mockops.presentation.api.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeploymentEventRequest(
        @NotBlank(message = "프로젝트 이름은 필수입니다")
        @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다")
        String projectName,

        @NotBlank(message = "배포 상태는 필수입니다")
        @Size(max = 50, message = "배포 상태는 50자 이하여야 합니다")
        String status,

        @NotBlank(message = "헬스 체크 URL은 필수입니다")
        @Size(max = 500, message = "헬스 체크 URL은 500자 이하여야 합니다")
        String healthCheckUrl,

        @Size(max = 20, message = "헬스 체크 주기는 20자 이하여야 합니다")
        String healthCheckInterval
) {
}