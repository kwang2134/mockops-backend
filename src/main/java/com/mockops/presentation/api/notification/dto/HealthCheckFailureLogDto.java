package com.mockops.presentation.api.notification.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.notification.entity.Notification;

import java.time.Instant;

/**
 * 헬스 체크 실패 로그 개별 항목 DTO
 */
public record HealthCheckFailureLogDto(
        Long notificationId,
        Instant failedAt,
        String failureReason,
        JsonNode metadata
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Notification 엔티티로부터 DTO 생성
     */
    public static HealthCheckFailureLogDto from(Notification notification) {
        JsonNode metadataNode = parseMetadata(notification.getMetadata());

        return new HealthCheckFailureLogDto(
                notification.getId(),
                notification.getCreatedAt(), // failedAt은 createdAt과 동일
                notification.getTitle(), // failureReason은 title을 활용
                metadataNode
        );
    }

    /**
     * metadata 문자열을 JsonNode로 파싱
     */
    private static JsonNode parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(metadata);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }
}
