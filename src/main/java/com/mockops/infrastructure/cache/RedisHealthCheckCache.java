package com.mockops.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Redis 기반 헬스 체크 작업 목록 관리
 * 주기별 Redis Set에 DomainServer 헬스 체크 정보를 저장/조회/삭제
 *
 * Key Pattern: healthcheck:jobs:{interval}
 * Value Format: {serverId}:{healthCheckPath}
 *
 * Example:
 * - Key: "healthcheck:jobs:5m"
 * - Values: ["123:/api/health", "456:/health", "789:/status"]
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthCheckCache {

    private static final String KEY_PREFIX = "healthcheck:jobs:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 헬스 체크 작업 추가
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로 (예: /api/health)
     */
    public void addHealthCheckJob(String interval, Long serverId, String healthCheckPath) {
        String key = generateKey(interval);
        String value = generateValue(serverId, healthCheckPath);

        redisTemplate.opsForSet().add(key, value);

        log.info("헬스 체크 작업 추가: interval={}, serverId={}, path={}", interval, serverId, healthCheckPath);
    }

    /**
     * 헬스 체크 작업 제거
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로
     */
    public void removeHealthCheckJob(String interval, Long serverId, String healthCheckPath) {
        String key = generateKey(interval);
        String value = generateValue(serverId, healthCheckPath);

        redisTemplate.opsForSet().remove(key, value);

        log.info("헬스 체크 작업 제거: interval={}, serverId={}, path={}", interval, serverId, healthCheckPath);
    }

    /**
     * 특정 주기의 모든 헬스 체크 작업 조회
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @return 헬스 체크 작업 정보 Set (형식: "serverId:path")
     */
    public Set<String> getAllHealthCheckJobs(String interval) {
        String key = generateKey(interval);
        Set<Object> members = redisTemplate.opsForSet().members(key);

        if (members == null || members.isEmpty()) {
            log.debug("헬스 체크 작업 없음: interval={}", interval);
            return new HashSet<>();
        }

        Set<String> jobs = new HashSet<>();
        for (Object member : members) {
            if (member instanceof String) {
                jobs.add((String) member);
            }
        }

        log.debug("헬스 체크 작업 조회: interval={}, count={}", interval, jobs.size());
        return jobs;
    }

    /**
     * 특정 서버의 헬스 체크 작업을 다른 주기로 이동 (Atomic Update)
     *
     * @param oldInterval 기존 주기
     * @param newInterval 새로운 주기
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로
     */
    public void moveHealthCheckJob(String oldInterval, String newInterval, Long serverId, String healthCheckPath) {
        // 기존 주기에서 제거
        removeHealthCheckJob(oldInterval, serverId, healthCheckPath);

        // 새로운 주기에 추가
        addHealthCheckJob(newInterval, serverId, healthCheckPath);

        log.info("헬스 체크 작업 이동: serverId={}, {} -> {}", serverId, oldInterval, newInterval);
    }

    /**
     * 특정 주기의 헬스 체크 작업 개수 조회
     *
     * @param interval 헬스 체크 주기
     * @return 작업 개수
     */
    public Long getHealthCheckJobCount(String interval) {
        String key = generateKey(interval);
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0L;
    }

    /**
     * 특정 주기의 모든 헬스 체크 작업 삭제
     *
     * @param interval 헬스 체크 주기
     */
    public void clearHealthCheckJobs(String interval) {
        String key = generateKey(interval);
        redisTemplate.delete(key);

        log.info("헬스 체크 작업 전체 삭제: interval={}", interval);
    }

    /**
     * Redis Key 생성
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     * @return Redis Key (예: "healthcheck:jobs:5m")
     */
    private String generateKey(String interval) {
        return KEY_PREFIX + interval;
    }

    /**
     * Redis Value 생성
     *
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로
     * @return Redis Value (예: "123:/api/health")
     */
    private String generateValue(Long serverId, String healthCheckPath) {
        return serverId + ":" + healthCheckPath;
    }

    /**
     * Redis Value 파싱 (serverId 추출)
     *
     * @param value Redis Value (예: "123:/api/health")
     * @return 서버 ID
     */
    public static Long parseServerId(String value) {
        if (value == null || !value.contains(":")) {
            throw new IllegalArgumentException("Invalid health check job value: " + value);
        }

        String[] parts = value.split(":", 2);
        return Long.parseLong(parts[0]);
    }

    /**
     * Redis Value 파싱 (healthCheckPath 추출)
     *
     * @param value Redis Value (예: "123:/api/health")
     * @return 헬스 체크 경로
     */
    public static String parseHealthCheckPath(String value) {
        if (value == null || !value.contains(":")) {
            throw new IllegalArgumentException("Invalid health check job value: " + value);
        }

        String[] parts = value.split(":", 2);
        return parts[1];
    }
}