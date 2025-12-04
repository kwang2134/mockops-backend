package com.mockops.presentation.api.notification.dto;

import java.util.List;

/**
 * 사용자 알림 페이지 응답 DTO
 */
public record UserNotificationPageResponse(
        List<NotificationDto> notifications,
        Integer totalUnreadCount,
        Long nextCursorId,
        Boolean hasNext
) {
    public static UserNotificationPageResponse of(
            List<NotificationDto> notifications,
            Integer totalUnreadCount,
            Long nextCursorId,
            Boolean hasNext
    ) {
        return new UserNotificationPageResponse(
                notifications,
                totalUnreadCount,
                nextCursorId,
                hasNext
        );
    }
}
