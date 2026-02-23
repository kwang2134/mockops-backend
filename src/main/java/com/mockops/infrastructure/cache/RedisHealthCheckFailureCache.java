package com.mockops.infrastructure.cache;

import com.mockops.domain.healthcheck.port.HealthCheckFailurePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 헬스 체크 실패 카운트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthCheckFailureCache implements HealthCheckFailurePort {

    private static final String FAILURE_COUNT_KEY_PREFIX = "healthcheck:failures:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public int incrementFailureCount(Long serverId, String interval) {
        String key = FAILURE_COUNT_KEY_PREFIX + serverId;
        Long count = redisTemplate.opsForValue().increment(key);

        // interval에 따라 적절한 TTL 설정
        // 3회 연속 실패를 감지하려면 (주기 * 4) 이상의 TTL이 필요
        long ttlHours = calculateTTL(interval);
        redisTemplate.expire(key, ttlHours, TimeUnit.HOURS);

        log.debug("실패 카운트 증가: serverId={}, interval={}, count={}, ttl={}시간",
                serverId, interval, count, ttlHours);

        return count != null ? count.intValue() : 0;
    }

    @Override
    public void resetFailureCount(Long serverId) {
        String key = FAILURE_COUNT_KEY_PREFIX + serverId;
        redisTemplate.delete(key);
        log.debug("실패 카운트 초기화: serverId={}", serverId);
    }

    /**
     * interval에 따라 적절한 TTL 계산
     *
     * @param interval 헬스 체크 주기
     * @return TTL (시간 단위)
     */
    private long calculateTTL(String interval) {
        return switch (interval) {
            case "5m" -> 1;   // 5분 * 4 = 20분 → 1시간이면 충분
            case "10m" -> 1;  // 10분 * 4 = 40분 → 1시간이면 충분
            case "30m" -> 2;  // 30분 * 4 = 2시간
            case "1h" -> 4;   // 1시간 * 4 = 4시간
            default -> 1;     // 기본값
        };
    }
}
