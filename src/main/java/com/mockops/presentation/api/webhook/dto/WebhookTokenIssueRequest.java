package com.mockops.presentation.api.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookTokenIssueRequest(
        @NotBlank(message = "Secret Key는 필수입니다")
        @Size(max = 255, message = "Secret Key는 255자 이하여야 합니다")
        String secretKey
) {
}