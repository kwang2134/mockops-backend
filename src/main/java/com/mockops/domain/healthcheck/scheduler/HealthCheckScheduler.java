package com.mockops.domain.healthcheck.scheduler;

import com.mockops.domain.healthcheck.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 도메인 서버 헬스 체크 스케줄러
 * 고정된 주기로 Redis Set에 등록된 서버들의 헬스 체크를 수행
 *
 * 지원 주기:
 * - 5분 (5m)
 * - 10분 (10m)
 * - 30분 (30m)
 * - 1시간 (1h)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;

    /**
     * 5분 주기 헬스 체크 스케줄러
     * fixedDelay: 이전 작업 완료 후 5분 대기
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5분 = 300,000ms, 초기 지연 1분
    public void healthCheck5Minutes() {
        log.info("=== 5분 주기 헬스 체크 시작 ===");
        try {
            healthCheckService.performBulkHealthCheck("5m");
        } catch (Exception e) {
            log.error("5분 주기 헬스 체크 중 오류 발생", e);
        }
        log.info("=== 5분 주기 헬스 체크 완료 ===");
    }

    /**
     * 10분 주기 헬스 체크 스케줄러 (기본)
     * fixedDelay: 이전 작업 완료 후 10분 대기
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 120000) // 10분 = 600,000ms, 초기 지연 2분
    public void healthCheck10Minutes() {
        log.info("=== 10분 주기 헬스 체크 시작 ===");
        try {
            healthCheckService.performBulkHealthCheck("10m");
        } catch (Exception e) {
            log.error("10분 주기 헬스 체크 중 오류 발생", e);
        }
        log.info("=== 10분 주기 헬스 체크 완료 ===");
    }

    /**
     * 30분 주기 헬스 체크 스케줄러
     * fixedDelay: 이전 작업 완료 후 30분 대기
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 180000) // 30분 = 1,800,000ms, 초기 지연 3분
    public void healthCheck30Minutes() {
        log.info("=== 30분 주기 헬스 체크 시작 ===");
        try {
            healthCheckService.performBulkHealthCheck("30m");
        } catch (Exception e) {
            log.error("30분 주기 헬스 체크 중 오류 발생", e);
        }
        log.info("=== 30분 주기 헬스 체크 완료 ===");
    }

    /**
     * 1시간 주기 헬스 체크 스케줄러
     * fixedDelay: 이전 작업 완료 후 1시간 대기
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 240000) // 1시간 = 3,600,000ms, 초기 지연 4분
    public void healthCheck1Hour() {
        log.info("=== 1시간 주기 헬스 체크 시작 ===");
        try {
            healthCheckService.performBulkHealthCheck("1h");
        } catch (Exception e) {
            log.error("1시간 주기 헬스 체크 중 오류 발생", e);
        }
        log.info("=== 1시간 주기 헬스 체크 완료 ===");
    }
}