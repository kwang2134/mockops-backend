package com.mockops.presentation.api.project.dto.project;

import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다")
        String description,

        @Size(max = 255, message = "Slack Webhook URL은 255자 이하여야 합니다")
        String slackWebhookUrl
) {
}
