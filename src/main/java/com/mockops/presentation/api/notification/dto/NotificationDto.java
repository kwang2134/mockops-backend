package com.mockops.presentation.api.notification.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.notification.entity.Notification;
import com.mockops.domain.notification.entity.NotificationType;

import java.time.Instant;
import java.util.Map;

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
        Object metadata,
        String redirectUrl
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Notification 엔티티로부터 DTO 생성
     */
    public static NotificationDto from(Notification notification) {
        Object metadataObject = parseMetadata(notification.getMetadata());
        String redirectUrl = generateRedirectUrl(notification.getType(), metadataObject);

        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                metadataObject,
                redirectUrl
        );
    }

    /**
     * metadata 문자열을 Object로 파싱
     * Object 타입으로 반환하면 Jackson이 실제 JSON 값을 그대로 직렬화함
     */
    private static Object parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }

        try {
            // readValue로 Object로 읽으면 Map이나 List 등 실제 값으로 변환됨
            return objectMapper.readValue(metadata, Object.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * 알림 타입과 metadata를 기반으로 redirectUrl 생성
     * 프론트엔드에서 알림 클릭 시 이동할 경로 생성
     */
    private static String generateRedirectUrl(NotificationType type, Object metadata) {
        // metadata를 Map으로 캐스팅하여 값 읽기
        Map<String, Object> metadataMap = (metadata instanceof Map)
                ? (Map<String, Object>) metadata
                : Map.of();

        return switch (type) {
            case MEMBER_INVITATION_RECEIVED -> {
                Long projectId = getLongValue(metadataMap, "projectId");
                yield projectId != null ? "/projects/" + projectId + "/invitations" : "/notifications";
            }
            case HEALTH_CHECK_FAILURE, SERVER_STATUS_CHANGED, MOCK_BULK_SUCCESS, MOCK_BULK_FAILURE -> {
                Long projectId = getLongValue(metadataMap, "projectId");
                Long serverId = getLongValue(metadataMap, "serverId");
                yield (projectId != null && serverId != null)
                        ? "/projects/" + projectId + "/servers/" + serverId
                        : "/notifications";
            }
            case MEMBER_INVITATION_ACCEPTED -> {
                Long projectId = getLongValue(metadataMap, "projectId");
                yield projectId != null ? "/projects/" + projectId + "/members" : "/notifications";
            }
            case SYSTEM_ANNOUNCEMENT -> "/notifications";
        };
    }

    /**
     * Map에서 Long 값을 안전하게 추출
     */
    private static Long getLongValue(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
