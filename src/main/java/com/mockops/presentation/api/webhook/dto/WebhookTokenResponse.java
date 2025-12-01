package com.mockops.presentation.api.webhook.dto;

public record WebhookTokenResponse(
        String jwtToken,
        Long expiresIn  // 만료 시간 (초 단위)
) {
    public static WebhookTokenResponse of(String jwtToken, Long expiresInSeconds) {
        return new WebhookTokenResponse(jwtToken, expiresInSeconds);
    }
}