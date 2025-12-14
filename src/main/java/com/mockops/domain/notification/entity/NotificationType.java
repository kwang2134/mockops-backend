package com.mockops.domain.notification.entity;

/**
 * 알림 유형
 */
public enum NotificationType {
    /**
     * WebHook 헬스 체크 실패 발생 (DomainServer 귀속)
     */
    HEALTH_CHECK_FAILURE,

    /**
     * 도메인 서버 상태 변경 발생 (DomainServer 귀속)
     */
    SERVER_STATUS_CHANGED,

    /**
     * 프로젝트 멤버 초대장 수신 (User 귀속)
     */
    MEMBER_INVITATION_RECEIVED,

    /**
     * 초대 수락/거절 결과 (User/DomainServer 귀속)
     */
    MEMBER_INVITATION_ACCEPTED,

    /**
     * Mock API 일괄 생성 작업 성공 (DomainServer 귀속)
     */
    MOCK_BULK_SUCCESS,

    /**
     * Mock API 일괄 생성 작업 실패 (DomainServer 귀속)
     */
    MOCK_BULK_FAILURE,

    /**
     * 서비스 전체 공지 (User 또는 Null 귀속)
     */
    SYSTEM_ANNOUNCEMENT
}
