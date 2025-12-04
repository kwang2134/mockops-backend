package com.mockops.presentation.api.notification;

import com.mockops.domain.notification.service.NotificationService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.notification.dto.HealthCheckFailureLogPageResponse;
import com.mockops.presentation.api.notification.dto.ServerNotificationSummaryResponse;
import com.mockops.presentation.api.notification.dto.UserNotificationPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자 알림 목록 조회 (커서 기반 페이징)
     */
    @GetMapping("/user")
    public ResponseEntity<UnifiedResponse<UserNotificationPageResponse>> getUserNotifications(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int pageSize,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("사용자 알림 조회 요청: userId={}, cursorId={}, pageSize={}", userId, cursorId, pageSize);

        UserNotificationPageResponse response = notificationService.getUserNotifications(
                userId,
                cursorId,
                pageSize
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("알림 읽음 처리 요청: notificationId={}, userId={}", notificationId, userId);

        notificationService.markNotificationAsRead(notificationId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("알림 삭제 요청: notificationId={}, userId={}", notificationId, userId);

        notificationService.deleteNotification(notificationId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 서버 알림 요약 조회
     */
    @GetMapping("/server/{serverId}")
    public ResponseEntity<UnifiedResponse<ServerNotificationSummaryResponse>> getServerNotificationSummary(
            @PathVariable Long serverId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("서버 알림 요약 조회 요청: serverId={}, userId={}", serverId, userId);

        ServerNotificationSummaryResponse response = notificationService.getServerNotificationSummary(
                serverId,
                userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 헬스 체크 실패 로그 조회
     */
    @GetMapping("/server/{serverId}/health-check-failures")
    public ResponseEntity<UnifiedResponse<HealthCheckFailureLogPageResponse>> getHealthCheckFailureLogs(
            @PathVariable Long serverId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int pageSize,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("헬스 체크 실패 로그 조회 요청: serverId={}, cursorId={}, pageSize={}, userId={}",
                serverId, cursorId, pageSize, userId);

        HealthCheckFailureLogPageResponse response = notificationService.getHealthCheckFailureLogs(
                serverId,
                cursorId,
                pageSize,
                userId
        );

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }
}
