package com.mockops.domain.notification.repository;

import com.mockops.domain.notification.entity.Notification;
import com.mockops.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 알림 Repository
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자 귀속 알림 조회 (커서 기반 페이징, 최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientUserId = :userId " +
           "AND (:cursorId IS NULL OR n.id < :cursorId) " +
           "ORDER BY n.id DESC")
    List<Notification> findByRecipientUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * 서버 귀속 알림 조회 (타입별, 최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.domainServerId = :serverId " +
           "AND n.type = :type " +
           "ORDER BY n.id DESC")
    List<Notification> findByDomainServerIdAndType(
            @Param("serverId") Long serverId,
            @Param("type") NotificationType type,
            Pageable pageable
    );

    /**
     * 서버 귀속 헬스 체크 실패 알림 조회 (커서 기반 페이징, 최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.domainServerId = :serverId " +
           "AND n.type = :type " +
           "AND (:cursorId IS NULL OR n.id < :cursorId) " +
           "ORDER BY n.id DESC")
    List<Notification> findHealthCheckFailuresByServerIdWithCursor(
            @Param("serverId") Long serverId,
            @Param("type") NotificationType type,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * 사용자별 미확인 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUserId = :userId AND n.isRead = false")
    Integer countUnreadByRecipientUserId(@Param("userId") Long userId);

    /**
     * 서버별 미확인 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.domainServerId = :serverId AND n.isRead = false")
    Integer countUnreadByDomainServerId(@Param("serverId") Long serverId);

    /**
     * 프로젝트 내 모든 서버의 미확인 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.domainServerId IN :serverIds AND n.isRead = false")
    Integer countUnreadByDomainServerIds(@Param("serverIds") List<Long> serverIds);

    /**
     * 서버별, 타입별 미확인 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.domainServerId = :serverId " +
           "AND n.type = :type AND n.isRead = false")
    Integer countUnreadByDomainServerIdAndType(
            @Param("serverId") Long serverId,
            @Param("type") NotificationType type
    );

    /**
     * 서버 귀속 알림 전체 조회 (타입 무관)
     */
    List<Notification> findByDomainServerId(Long serverId);
}
