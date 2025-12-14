package com.mockops.presentation.api.notification.dto;

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
        Object metadata  // JsonNode -> Object로 변경하여 실제 JSON 값이 직렬화되도록 함
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Notification 엔티티로부터 DTO 생성
     */
    public static HealthCheckFailureLogDto from(Notification notification) {
        Object metadataObject = parseMetadata(notification.getMetadata());

        return new HealthCheckFailureLogDto(
                notification.getId(),
                notification.getCreatedAt(), // failedAt은 createdAt과 동일
                notification.getTitle(), // failureReason은 title을 활용
                metadataObject
        );
    }

    /**
     * metadata 문자열을 Object로 파싱
     * Object 타입으로 반환하면 Jackson이 실제 JSON 값을 그대로 직렬화함
     */
    private static Object parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            // readValue로 Object로 읽으면 Map이나 List 등 실제 값으로 변환됨
            return objectMapper.readValue(metadata, Object.class);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }
}
