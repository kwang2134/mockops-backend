package com.mockops.presentation.api.notification.dto;

import com.mockops.domain.notification.entity.NotificationType;

import java.util.List;

/**
 * 서버 타입별 알림 요약 DTO
 */
public record ServerNotificationSummaryDto(
        NotificationType type,
        List<NotificationDto> latestNotifications,
        Integer unreadCount
) {
    public static ServerNotificationSummaryDto of(
            NotificationType type,
            List<NotificationDto> latestNotifications,
            Integer unreadCount
    ) {
        return new ServerNotificationSummaryDto(
                type,
                latestNotifications,
                unreadCount
        );
    }
}
