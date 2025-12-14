package com.mockops.infrastructure.cache;

import com.mockops.domain.project.infrastructure.CorsOriginCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 CORS Origin 캐시 구현체
 * Infrastructure 계층에서 외부 기술(Redis) 의존성을 캡슐화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCorsOriginCache implements CorsOriginCachePort {

    private static final String CACHE_KEY_PREFIX = "project:cors:";
    private static final long CACHE_TTL_HOURS = 24; // 24시간 캐시 유지
    private static final String RECOVERY_MODE_KEY = "OPS:RECOVERY_MODE";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Set<String> getAllowedOrigins(Long projectId) {
        // 복구 모드 확인 - 활성화 시 캐시를 신뢰하지 않고 무조건 Cache Miss 처리
        if (isRecoveryModeActive()) {
            log.warn("Recovery Mode Active. Bypassing cache read and forcing Cache Miss for CORS origins.");
            return new HashSet<>();
        }

        String cacheKey = generateCacheKey(projectId);
        Set<Object> cachedOrigins = redisTemplate.opsForSet().members(cacheKey);

        if (cachedOrigins == null || cachedOrigins.isEmpty()) {
            log.debug("Cache miss for project CORS origins: projectId={}", projectId);
            return new HashSet<>();
        }

        Set<String> origins = new HashSet<>();
        for (Object origin : cachedOrigins) {
            if (origin instanceof String) {
                origins.add((String) origin);
            }
        }

        log.debug("Cache hit for project CORS origins: projectId={}, size={}", projectId, origins.size());
        return origins;
    }

    @Override
    public void cacheAllowedOrigins(Long projectId, Set<String> origins) {
        if (origins == null || origins.isEmpty()) {
            log.debug("No origins to cache for project: projectId={}", projectId);
            evictCache(projectId);
            return;
        }

        String cacheKey = generateCacheKey(projectId);

        // 기존 캐시 삭제
        redisTemplate.delete(cacheKey);

        // 새로운 값 저장
        redisTemplate.opsForSet().add(cacheKey, origins.toArray());

        // TTL 설정
        redisTemplate.expire(cacheKey, CACHE_TTL_HOURS, TimeUnit.HOURS);

        log.info("Cached CORS origins for project: projectId={}, count={}", projectId, origins.size());
    }

    @Override
    public void evictCache(Long projectId) {
        String cacheKey = generateCacheKey(projectId);
        redisTemplate.delete(cacheKey);
        log.info("Evicted CORS origin cache for project: projectId={}", projectId);
    }

    @Override
    public boolean isOriginAllowed(Long projectId, String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }

        // 복구 모드 확인 - 활성화 시 캐시를 신뢰하지 않고 false 반환 (DB 조회 유도)
        if (isRecoveryModeActive()) {
            log.warn("Recovery Mode Active. Bypassing cache for origin check: projectId={}, origin={}", projectId, origin);
            return false;
        }

        String cacheKey = generateCacheKey(projectId);
        Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, origin);

        return Boolean.TRUE.equals(isMember);
    }

    private String generateCacheKey(Long projectId) {
        return CACHE_KEY_PREFIX + projectId;
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
