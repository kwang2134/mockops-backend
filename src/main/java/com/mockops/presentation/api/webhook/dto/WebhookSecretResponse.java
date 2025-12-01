package com.mockops.presentation.api.webhook.dto;

import com.mockops.domain.webhook.entity.WebhookSecret;

public record WebhookSecretResponse(
        Long projectId,
        Boolean isActive,
        String secretKey  // 새로 발급된 Secret Key (재발급 시에만 포함)
) {
    /**
     * WebhookSecret 엔티티를 응답 DTO로 변환 (Secret Key 숨김)
     */
    public static WebhookSecretResponse from(WebhookSecret webhookSecret) {
        return new WebhookSecretResponse(
                webhookSecret.getProjectId(),
                webhookSecret.getIsActive(),
                null  // Secret Key는 숨김
        );
    }

    /**
     * WebhookSecret 엔티티를 응답 DTO로 변환 (Secret Key 노출 - 재발급 시에만 사용)
     */
    public static WebhookSecretResponse fromWithSecretKey(WebhookSecret webhookSecret, String decryptedSecretKey) {
        return new WebhookSecretResponse(
                webhookSecret.getProjectId(),
                webhookSecret.getIsActive(),
                decryptedSecretKey
        );
    }
}