package com.mockops.domain.notification.service;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.repository.DomainServerRepository;
import com.mockops.domain.notification.entity.Notification;
import com.mockops.domain.notification.entity.NotificationType;
import com.mockops.domain.notification.repository.NotificationRepository;
import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.repository.ProjectMemberRepository;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.notification.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DomainServerRepository domainServerRepository;
    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public Notification createNotification(
            Long recipientUserId,
            Long domainServerId,
            NotificationType type,
            String title,
            String message,
            String metadata
    ) {
        // recipientUserId 또는 domainServerId 중 최소 하나는 반드시 값을 가져야 함
        if (recipientUserId == null && domainServerId == null) {
            throw ErrorCode.BAD_REQUEST.serviceException(
                    "알림 생성 실패: recipientUserId 또는 domainServerId 중 최소 하나는 필수입니다."
            );
        }

        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .domainServerId(domainServerId)
                .type(type)
                .title(title)
                .message(message)
                .metadata(metadata)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 사용자 알림 조회 (커서 기반 페이징)
     */
    public UserNotificationPageResponse getUserNotifications(Long userId, Long cursorId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize + 1); // hasNext 확인용 +1

        List<Notification> notifications = notificationRepository.findByRecipientUserIdWithCursor(
                userId,
                cursorId,
                pageable
        );

        boolean hasNext = notifications.size() > pageSize;
        if (hasNext) {
            notifications = notifications.subList(0, pageSize);
        }

        List<NotificationDto> notificationDtos = notifications.stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());

        Long nextCursorId = hasNext && !notifications.isEmpty()
                ? notifications.get(notifications.size() - 1).getId()
                : null;

        Integer totalUnreadCount = notificationRepository.countUnreadByRecipientUserId(userId);

        return UserNotificationPageResponse.of(
                notificationDtos,
                totalUnreadCount,
                nextCursorId,
                hasNext
        );
    }

    /**
     * 서버 알림 요약 조회
     */
    public ServerNotificationSummaryResponse getServerNotificationSummary(Long serverId, Long currentUserId) {
        DomainServer server = domainServerRepository.findById(serverId)
                .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException(
                        "서버를 찾을 수 없습니다. serverId=" + serverId
                ));

        // 프로젝트 멤버 권한 확인
        validateProjectMember(server.getProjectId(), currentUserId);

        // 서버 관련 알림 타입들
        List<NotificationType> serverNotificationTypes = Arrays.asList(
                NotificationType.HEALTH_CHECK_FAILURE,
                NotificationType.SERVER_STATUS_CHANGED,
                NotificationType.MOCK_BULK_SUCCESS,
                NotificationType.MOCK_BULK_FAILURE
        );

        List<ServerNotificationSummaryDto> summaries = serverNotificationTypes.stream()
                .map(type -> {
                    // 각 타입별 최근 10개 알림 조회
                    List<Notification> latestNotifications = notificationRepository
                            .findByDomainServerIdAndType(serverId, type, PageRequest.of(0, 10));

                    List<NotificationDto> notificationDtos = latestNotifications.stream()
                            .map(NotificationDto::from)
                            .collect(Collectors.toList());

                    // 해당 타입의 미확인 알림 개수
                    Integer unreadCount = notificationRepository
                            .countUnreadByDomainServerIdAndType(serverId, type);

                    return ServerNotificationSummaryDto.of(type, notificationDtos, unreadCount);
                })
                .collect(Collectors.toList());

        Integer totalUnreadCount = notificationRepository.countUnreadByDomainServerId(serverId);

        return ServerNotificationSummaryResponse.of(
                server.getName(),
                totalUnreadCount,
                summaries
        );
    }

    /**
     * 헬스 체크 실패 로그 조회
     */
    public HealthCheckFailureLogPageResponse getHealthCheckFailureLogs(
            Long serverId,
            Long cursorId,
            int pageSize,
            Long currentUserId
    ) {
        DomainServer server = domainServerRepository.findById(serverId)
                .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException(
                        "서버를 찾을 수 없습니다. serverId=" + serverId
                ));

        // 프로젝트 멤버 권한 확인
        validateProjectMember(server.getProjectId(), currentUserId);

        Pageable pageable = PageRequest.of(0, pageSize + 1); // hasNext 확인용 +1

        List<Notification> notifications = notificationRepository
                .findHealthCheckFailuresByServerIdWithCursor(
                        serverId,
                        NotificationType.HEALTH_CHECK_FAILURE,
                        cursorId,
                        pageable
                );

        boolean hasNext = notifications.size() > pageSize;
        if (hasNext) {
            notifications = notifications.subList(0, pageSize);
        }

        List<HealthCheckFailureLogDto> failureLogs = notifications.stream()
                .map(HealthCheckFailureLogDto::from)
                .collect(Collectors.toList());

        Long nextCursorId = hasNext && !notifications.isEmpty()
                ? notifications.get(notifications.size() - 1).getId()
                : null;

        return HealthCheckFailureLogPageResponse.of(
                failureLogs,
                nextCursorId,
                hasNext
        );
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ErrorCode.BAD_REQUEST.serviceException(
                        "알림을 찾을 수 없습니다. notificationId=" + notificationId
                ));

        // 권한 확인
        validateNotificationAccess(notification, currentUserId);

        notification.markAsRead();
        log.info("알림 읽음 처리 완료: notificationId={}, userId={}", notificationId, currentUserId);
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ErrorCode.BAD_REQUEST.serviceException(
                        "알림을 찾을 수 없습니다. notificationId=" + notificationId
                ));

        // 권한 확인
        validateNotificationAccess(notification, currentUserId);

        notificationRepository.delete(notification);
        log.info("알림 삭제 완료: notificationId={}, userId={}", notificationId, currentUserId);
    }

    /**
     * 서버별 미확인 알림 개수 조회
     */
    public Integer getUnreadCountByServerId(Long serverId) {
        return notificationRepository.countUnreadByDomainServerId(serverId);
    }

    /**
     * 프로젝트 내 모든 서버의 미확인 알림 개수 조회
     */
    public Integer getUnreadCountByProject(Project project) {
        List<DomainServer> servers = domainServerRepository.findByProjectId(project.getId());
        List<Long> serverIds = servers.stream()
                .map(DomainServer::getId)
                .collect(Collectors.toList());

        if (serverIds.isEmpty()) {
            return 0;
        }

        return notificationRepository.countUnreadByDomainServerIds(serverIds);
    }

    /**
     * 알림 접근 권한 확인
     */
    private void validateNotificationAccess(Notification notification, Long currentUserId) {
        // 사용자 귀속 알림인 경우
        if (notification.getRecipientUserId() != null) {
            if (!notification.getRecipientUserId().equals(currentUserId)) {
                throw ErrorCode.PERMISSION_DENIED.serviceException(
                        "해당 알림에 접근할 권한이 없습니다."
                );
            }
        }
        // 서버 귀속 알림인 경우
        else if (notification.getDomainServerId() != null) {
            DomainServer server = domainServerRepository.findById(notification.getDomainServerId())
                    .orElseThrow(() -> ErrorCode.DOMAIN_SERVER_NOT_FOUND.domainException(
                            "서버를 찾을 수 없습니다."
                    ));
            validateProjectMember(server.getProjectId(), currentUserId);
        }
    }

    /**
     * 프로젝트 멤버 확인
     */
    private void validateProjectMember(Long projectId, Long userId) {
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw ErrorCode.PERMISSION_DENIED.serviceException(
                    "프로젝트 멤버만 접근 가능합니다."
            );
        }
    }
}
