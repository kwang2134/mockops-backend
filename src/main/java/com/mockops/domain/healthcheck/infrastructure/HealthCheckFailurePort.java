package com.mockops.domain.healthcheck.infrastructure;

/**
 * 헬스 체크 실패 카운트 관리 포트
 */
public interface HealthCheckFailurePort {

    /**
     * 서버의 연속 실패 카운트 증가
     *
     * @param serverId 서버 ID
     * @param interval 헬스 체크 주기 (TTL 계산용)
     * @return 현재 실패 카운트
     */
    int incrementFailureCount(Long serverId, String interval);

    /**
     * 서버의 연속 실패 카운트 초기화
     *
     * @param serverId 서버 ID
     */
    void resetFailureCount(Long serverId);
}