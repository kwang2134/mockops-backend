package com.mockops.presentation.api.notification.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.notification.entity.Notification;
import com.mockops.domain.notification.entity.NotificationType;

import java.time.Instant;

/**
 * 개별 알림 항목 DTO
 */
public record NotificationDto(
        Long notificationId,
        NotificationType type,
        String title,
        String message,
        Boolean isRead,
        Instant createdAt,
        JsonNode metadata,
        String redirectUrl
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Notification 엔티티로부터 DTO 생성
     */
    public static NotificationDto from(Notification notification) {
        JsonNode metadataNode = parseMetadata(notification.getMetadata());
        String redirectUrl = generateRedirectUrl(notification.getType(), metadataNode);

        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                metadataNode,
                redirectUrl
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

    /**
     * 알림 타입과 metadata를 기반으로 redirectUrl 생성
     * 프론트엔드에서 알림 클릭 시 이동할 경로 생성
     */
    private static String generateRedirectUrl(NotificationType type, JsonNode metadata) {
        return switch (type) {
            case MEMBER_INVITATION_RECEIVED -> {
                Long projectId = metadata.has("projectId") ? metadata.get("projectId").asLong() : null;
                yield projectId != null ? "/projects/" + projectId + "/invitations" : "/notifications";
            }
            case HEALTH_CHECK_FAILURE, SERVER_STATUS_CHANGED, MOCK_BULK_SUCCESS, MOCK_BULK_FAILURE -> {
                Long projectId = metadata.has("projectId") ? metadata.get("projectId").asLong() : null;
                Long serverId = metadata.has("serverId") ? metadata.get("serverId").asLong() : null;
                yield (projectId != null && serverId != null)
                        ? "/projects/" + projectId + "/servers/" + serverId
                        : "/notifications";
            }
            case MEMBER_INVITATION_ACCEPTED -> {
                Long projectId = metadata.has("projectId") ? metadata.get("projectId").asLong() : null;
                yield projectId != null ? "/projects/" + projectId + "/members" : "/notifications";
            }
            case SYSTEM_ANNOUNCEMENT -> "/notifications";
        };
    }
}
