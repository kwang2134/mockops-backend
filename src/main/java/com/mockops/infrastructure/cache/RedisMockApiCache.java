package com.mockops.infrastructure.cache;

import com.mockops.domain.mock.dto.MockCacheDto;
import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.infrastructure.MockApiCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 Mock API 캐시 구현
 * 캐시 키 형식: MOCK:{projectId}:{serverName}:{httpMethod}:{endpointPath}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMockApiCache implements MockApiCachePort {

    private static final String CACHE_PREFIX = "MOCK";
    private static final Duration CACHE_TTL = Duration.ofHours(24); // 24시간
    private static final String RECOVERY_MODE_KEY = "OPS:RECOVERY_MODE";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void cacheMockApi(Long projectId, String serverName, HttpMethod httpMethod,
                            String endpointPath, MockCacheDto cacheDto) {
        String key = generateCacheKey(projectId, serverName, httpMethod, endpointPath);
        try {
            redisTemplate.opsForValue().set(key, cacheDto, CACHE_TTL);
            log.debug("Mock API 캐시 저장: key={}", key);
        } catch (Exception e) {
            log.error("Mock API 캐시 저장 실패: key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public Optional<MockCacheDto> getMockApiFromCache(Long projectId, String serverName,
                                                      HttpMethod httpMethod, String endpointPath) {
        // 복구 모드 확인 - 활성화 시 캐시를 신뢰하지 않고 무조건 Cache Miss 처리
        if (isRecoveryModeActive()) {
            log.warn("Recovery Mode Active. Bypassing cache read and forcing Cache Miss for Mock API.");
            return Optional.empty();
        }

        String key = generateCacheKey(projectId, serverName, httpMethod, endpointPath);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof MockCacheDto) {
                log.debug("Mock API 캐시 히트: key={}", key);
                return Optional.of((MockCacheDto) cached);
            }
            log.debug("Mock API 캐시 미스: key={}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Mock API 캐시 조회 실패: key={}, error={}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void evictMockApi(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath) {
        String key = generateCacheKey(projectId, serverName, httpMethod, endpointPath);
        try {
            redisTemplate.delete(key);
            log.debug("Mock API 캐시 삭제: key={}", key);
        } catch (Exception e) {
            log.error("Mock API 캐시 삭제 실패: key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public void evictAllMockApisForServer(Long projectId, String serverName) {
        String pattern = String.format("%s:%d:%s:*", CACHE_PREFIX, projectId, serverName);
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("서버의 모든 Mock API 캐시 삭제: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("서버의 모든 Mock API 캐시 삭제 실패: pattern={}, error={}", pattern, e.getMessage());
        }
    }

    /**
     * 캐시 키 생성
     * 형식: MOCK:{projectId}:{serverName}:{httpMethod}:{endpointPath}
     */
    private String generateCacheKey(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath) {
        return String.format("%s:%d:%s:%s:%s", CACHE_PREFIX, projectId, serverName, httpMethod, endpointPath);
    }

    /**
     * Redis 복구 모드 활성화 여부 확인
     * OPS:RECOVERY_MODE 키가 존재하면 복구 모드로 간주
     */
    private boolean isRecoveryModeActive() {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(RECOVERY_MODE_KEY));
        } catch (Exception e) {
            log.error("Recovery mode 확인 실패: error={}", e.getMessage());
            // Redis 연결 장애 시에도 안전하게 처리 (복구 모드 아님으로 간주)
            return false;
        }
    }
}
