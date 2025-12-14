package com.mockops.domain.notification.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 엔티티
 * 서비스 전체에서 발생하는 중요 이벤트(헬스 체크 실패, 멤버 초대, 시스템 공지 등)를 기록합니다.
 * 알림은 특정 userId 또는 domainServerId에 귀속됩니다.
 */
@Entity
@Table(name = "service_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림을 받을 사용자 ID (사용자에게 직접 보내는 알림인 경우)
     * recipientUserId 또는 domainServerId 중 최소 하나는 반드시 값을 가져야 함 (서비스 레이어에서 검증)
     */
    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    /**
     * 알림이 귀속된 대상 서버 ID (프로젝트 관련 알림인 경우)
     * recipientUserId 또는 domainServerId 중 최소 하나는 반드시 값을 가져야 함 (서비스 레이어에서 검증)
     */
    @Column(name = "domain_server_id")
    private Long domainServerId;

    /**
     * 알림 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * 알림 제목
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 상세 알림 내용
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * 사용자가 확인했는지 여부 (기본값: false)
     */
    @Column(nullable = false)
    private Boolean isRead = false;

    /**
     * 이벤트에 필요한 추가 데이터
     * (예: 초대 사용자 ID, 실패 Endpoint URL 등 JSON String)
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Builder
    public Notification(
            Long recipientUserId,
            Long domainServerId,
            NotificationType type,
            String title,
            String message,
            Boolean isRead,
            String metadata
    ) {
        this.recipientUserId = recipientUserId;
        this.domainServerId = domainServerId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead != null ? isRead : false;
        this.metadata = metadata;
    }

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 알림의 유효성 검증
     * recipientUserId 또는 domainServerId 중 최소 하나는 반드시 값을 가져야 함
     */
    public boolean isValid() {
        return recipientUserId != null || domainServerId != null;
    }
}
