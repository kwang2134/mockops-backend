package com.mockops.presentation.api.notification.dto;

import java.util.List;

/**
 * 서버 알림 요약 응답 DTO
 */
public record ServerNotificationSummaryResponse(
        String serverName,
        Integer totalUnreadCount,
        List<ServerNotificationSummaryDto> summaries
) {
    public static ServerNotificationSummaryResponse of(
            String serverName,
            Integer totalUnreadCount,
            List<ServerNotificationSummaryDto> summaries
    ) {
        return new ServerNotificationSummaryResponse(
                serverName,
                totalUnreadCount,
                summaries
        );
    }
}
