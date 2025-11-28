package com.mockops.config;

import com.mockops.domain.project.infrastructure.CorsOriginCachePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

/**
 * 테스트 환경에서 Redis를 사용하지 않고 Mock으로 대체
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public CorsOriginCachePort testCorsOriginCachePort() {
        return new CorsOriginCachePort() {
            private final Set<String> mockCache = new HashSet<>();

            @Override
            public Set<String> getAllowedOrigins(Long projectId) {
                return new HashSet<>(mockCache);
            }

            @Override
            public void cacheAllowedOrigins(Long projectId, Set<String> origins) {
                mockCache.clear();
                mockCache.addAll(origins);
            }

            @Override
            public void evictCache(Long projectId) {
                mockCache.clear();
            }

            @Override
            public boolean isOriginAllowed(Long projectId, String origin) {
                return mockCache.contains(origin);
            }
        };
    }
}
