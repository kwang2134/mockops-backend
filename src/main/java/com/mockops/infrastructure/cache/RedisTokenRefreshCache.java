package com.mockops.infrastructure.cache;

import com.mockops.domain.user.infrastructure.TokenRefreshCachePort;
import com.mockops.presentation.api.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenRefreshCache  implements TokenRefreshCachePort {

    private static final String CACHE_KEY_PREFIX = "token_grace:";
    private static final long GRACE_PERIOD_DURATION = 30; // 30초간 캐시 유지
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void saveWithGracePeriod(String oldToken, TokenResponse newTokenResponse) {
        String key = CACHE_KEY_PREFIX + oldToken;
        try {
            // 유예 기간(TTL)과 함께 새 토큰 정보를 저장
            redisTemplate.opsForValue().set(
                    key,
                    newTokenResponse,
                    Duration.ofSeconds(GRACE_PERIOD_DURATION)
            );
        } catch (Exception e) {
            // 인프라 장애가 비즈니스 로직을 중단시키지 않도록 예외 처리
            log.error("Failed to save grace period token to Redis: {}", e.getMessage());
        }
    }

    @Override
    public Optional<TokenResponse> getNewTokenIfInGracePeriod(String oldToken) {
        String key = CACHE_KEY_PREFIX + oldToken;
        try {
            Object cachedToken = redisTemplate.opsForValue().get(key);

            if (cachedToken instanceof TokenResponse) {
                log.info("token refresh 캐시 히트");
                return Optional.of((TokenResponse) cachedToken);
            }
            log.info("token refresh 캐시 미스");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch grace period token from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
