package com.mockops.domain.healthcheck.port;

import java.util.Set;

public interface HealthCheckCachePort {

    /**
     * 헬스 체크 작업 추가
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로 (예: /api/health)
     */
    public void addHealthCheckJob(String interval, Long serverId, String healthCheckPath);


    /**
     * 헬스 체크 작업 제거
     *
     * @param interval        헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @param serverId        서버 ID
     * @param healthCheckPath 헬스 체크 경로
     */
    public void removeHealthCheckJob(String interval, Long serverId, String healthCheckPath);


    /**
     * 특정 주기의 모든 헬스 체크 작업 조회
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @return 헬스 체크 작업 정보 Set (형식: "serverId:path")
     */
    public Set<String> getAllHealthCheckJobs(String interval);


    /**
     * 특정 서버의 헬스 체크 작업을 다른 주기로 이동 (Atomic Update)
     *
     * @param oldInterval     기존 주기
     * @param newInterval     새로운 주기
     * @param serverId        서버 ID
     * @param healthCheckPath 헬스 체크 경로
     */
    public void moveHealthCheckJob(String oldInterval, String newInterval, Long serverId, String healthCheckPath);

    /**
     * 특정 주기의 헬스 체크 작업 개수 조회
     *
     * @param interval 헬스 체크 주기
     * @return 작업 개수
     */
    public Long getHealthCheckJobCount(String interval);


    /**
     * 특정 주기의 모든 헬스 체크 작업 삭제
     *
     * @param interval 헬스 체크 주기
     */
    public void clearHealthCheckJobs(String interval);



}
