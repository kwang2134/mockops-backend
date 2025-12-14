package com.mockops.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.mock.dto.MockCacheDto;
import com.mockops.domain.mock.infrastructure.MockApiCachePort;
import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.project.infrastructure.CorsOriginCachePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.Optional;
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

    @Bean
    @Primary
    public MockApiCachePort testMockApiCachePort() {
        return new MockApiCachePort() {
            @Override
            public void cacheMockApi(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath, MockCacheDto cacheDto) {
                // Mock implementation - do nothing
            }

            @Override
            public Optional<MockCacheDto> getMockApiFromCache(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath) {
                return Optional.empty();
            }

            @Override
            public void evictMockApi(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath) {
                // Mock implementation - do nothing
            }

            @Override
            public void evictAllMockApisForServer(Long projectId, String serverName) {
                // Mock implementation - do nothing
            }
        };
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public WebClient healthCheckWebClient() {
        return WebClient.builder().build();
    }
}
