package com.mockops.presentation.api.project.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank(message = "프로젝트 이름은 필수입니다")
        @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다")
        String name,

        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다")
        String description,

        @Size(max = 255, message = "Slack Webhook URL은 255자 이하여야 합니다")
        String slackWebhookUrl
) {
}
