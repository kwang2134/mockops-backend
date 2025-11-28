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

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Set<String> getAllowedOrigins(Long projectId) {
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

        String cacheKey = generateCacheKey(projectId);
        Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, origin);

        return Boolean.TRUE.equals(isMember);
    }

    private String generateCacheKey(Long projectId) {
        return CACHE_KEY_PREFIX + projectId;
    }
}
