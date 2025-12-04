package com.mockops.domain.healthcheck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.mock.repository.DomainServerRepository;
import com.mockops.domain.notification.entity.NotificationType;
import com.mockops.domain.notification.service.NotificationService;
import com.mockops.domain.notification.service.SlackNotificationService;
import com.mockops.infrastructure.cache.RedisHealthCheckCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 도메인 서버 헬스 체크 서비스
 * 스케줄러에서 호출하여 서버 상태를 확인하고 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private static final String FAILURE_COUNT_KEY_PREFIX = "healthcheck:failures:";
    private static final int FAILURE_THRESHOLD = 3; // 3회 연속 실패 시 ERROR 상태로 전환

    private final WebClient healthCheckWebClient;
    private final DomainServerRepository domainServerRepository;
    private final RedisHealthCheckCache redisHealthCheckCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SlackNotificationService slackNotificationService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * 특정 주기의 모든 헬스 체크 수행 (벌크 처리)
     *
     * @param interval 헬스 체크 주기 (5m, 10m, 30m, 1h)
     */
    public void performBulkHealthCheck(String interval) {
        log.info("헬스 체크 벌크 작업 시작: interval={}", interval);

        Set<String> jobs = redisHealthCheckCache.getAllHealthCheckJobs(interval);

        if (jobs.isEmpty()) {
            log.debug("헬스 체크 대상 없음: interval={}", interval);
            return;
        }

        log.info("헬스 체크 대상: interval={}, count={}", interval, jobs.size());

        for (String job : jobs) {
            try {
                Long serverId = RedisHealthCheckCache.parseServerId(job);
                String healthCheckPath = RedisHealthCheckCache.parseHealthCheckPath(job);

                performHealthCheck(serverId, healthCheckPath, interval);
            } catch (Exception e) {
                log.error("헬스 체크 처리 중 오류 발생: job={}, error={}", job, e.getMessage(), e);
            }
        }

        log.info("헬스 체크 벌크 작업 완료: interval={}, processed={}", interval, jobs.size());
    }

    /**
     * 개별 서버 헬스 체크 수행
     *
     * @param serverId 서버 ID
     * @param healthCheckPath 헬스 체크 경로
     * @param interval 헬스 체크 주기 (TTL 계산용)
     */
    @Transactional
    public void performHealthCheck(Long serverId, String healthCheckPath, String interval) {
        DomainServer server = domainServerRepository.findById(serverId).orElse(null);

        if (server == null) {
            log.warn("헬스 체크 대상 서버를 찾을 수 없음: serverId={}", serverId);
            return;
        }

        // 헬스 체크 URL 구성
        String fullUrl = constructHealthCheckUrl(server, healthCheckPath);

        log.debug("헬스 체크 시작: serverId={}, url={}, interval={}", serverId, fullUrl, interval);

        // WebClient로 HTTP GET 요청
        boolean isHealthy = checkHealth(fullUrl);

        // 결과에 따라 서버 상태 업데이트
        updateServerStatus(server, isHealthy, interval);

        // lastCheckedAt 업데이트
        server.updateLastCheckedAt();

        log.info("헬스 체크 완료: serverId={}, url={}, isHealthy={}, status={}",
                serverId, fullUrl, isHealthy, server.getStatus());
    }

    /**
     * WebClient를 사용한 헬스 체크 수행
     *
     * @param url 헬스 체크 URL
     * @return 헬스 체크 성공 여부
     */
    private boolean checkHealth(String url) {
        try {
            HttpStatusCode status = healthCheckWebClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode())
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> {
                        log.warn("헬스 체크 요청 실패: url={}, error={}", url, e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            boolean isHealthy = status != null && status.is2xxSuccessful();
            log.debug("헬스 체크 응답: url={}, status={}, isHealthy={}", url, status, isHealthy);

            return isHealthy;
        } catch (Exception e) {
            log.error("헬스 체크 중 예외 발생: url={}, error={}", url, e.getMessage());
            return false;
        }
    }

    /**
     * 헬스 체크 결과에 따라 서버 상태 업데이트
     *
     * @param server 도메인 서버
     * @param isHealthy 헬스 체크 성공 여부
     * @param interval 헬스 체크 주기 (TTL 계산용)
     */
    private void updateServerStatus(DomainServer server, boolean isHealthy, String interval) {
        ServerStatus previousStatus = server.getStatus();

        if (isHealthy) {
            // 헬스 체크 성공 시
            handleHealthyServer(server, previousStatus);
        } else {
            // 헬스 체크 실패 시
            handleUnhealthyServer(server, previousStatus, interval);
        }
    }

    /**
     * 헬스 체크 성공 시 처리
     */
    private void handleHealthyServer(DomainServer server, ServerStatus previousStatus) {
        // 실패 카운트 초기화
        resetFailureCount(server.getId());

        // ERROR 상태에서 DEPLOYED로 복구
        if (previousStatus == ServerStatus.ERROR) {
            server.updateStatus(ServerStatus.DEPLOYED);
            log.info("서버 상태 복구: serverId={}, ERROR -> DEPLOYED", server.getId());

            // Slack 알림 전송 (ERROR -> DEPLOYED)
            slackNotificationService.sendStatusChangeNotification(server, previousStatus, ServerStatus.DEPLOYED);

            // 서버 상태 변경 알림 생성 (복구)
            createServerStatusChangeNotification(server, previousStatus, ServerStatus.DEPLOYED);
        }
    }

    /**
     * 헬스 체크 실패 시 처리
     *
     * @param server 도메인 서버
     * @param previousStatus 이전 상태
     * @param interval 헬스 체크 주기 (TTL 계산용)
     */
    private void handleUnhealthyServer(DomainServer server, ServerStatus previousStatus, String interval) {
        // 실패 카운트 증가 (interval에 따라 적절한 TTL 설정)
        int failureCount = incrementFailureCount(server.getId(), interval);

        log.warn("헬스 체크 실패: serverId={}, interval={}, failureCount={}/{}",
                server.getId(), interval, failureCount, FAILURE_THRESHOLD);

        // 헬스 체크 실패 알림 생성
        createHealthCheckFailureNotification(server, failureCount);

        // 3회 연속 실패 시 ERROR 상태로 전환
        if (failureCount >= FAILURE_THRESHOLD && previousStatus == ServerStatus.DEPLOYED) {
            server.updateStatus(ServerStatus.ERROR);
            log.error("서버 상태 ERROR로 전환: serverId={}, DEPLOYED -> ERROR, failureCount={}",
                    server.getId(), failureCount);

            // Slack 알림 전송 (DEPLOYED -> ERROR)
            slackNotificationService.sendStatusChangeNotification(server, previousStatus, ServerStatus.ERROR);

            // 서버 상태 변경 알림 생성
            createServerStatusChangeNotification(server, previousStatus, ServerStatus.ERROR);
        }
    }

    /**
     * 서버의 연속 실패 카운트 증가
     *
     * @param serverId 서버 ID
     * @param interval 헬스 체크 주기 (TTL 계산용)
     * @return 현재 실패 카운트
     */
    private int incrementFailureCount(Long serverId, String interval) {
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

    /**
     * 서버의 연속 실패 카운트 초기화
     *
     * @param serverId 서버 ID
     */
    private void resetFailureCount(Long serverId) {
        String key = FAILURE_COUNT_KEY_PREFIX + serverId;
        redisTemplate.delete(key);
    }

    /**
     * 헬스 체크 URL 구성
     *
     * @param server 도메인 서버
     * @param healthCheckPath 헬스 체크 경로
     * @return 완전한 헬스 체크 URL
     */
    private String constructHealthCheckUrl(DomainServer server, String healthCheckPath) {
        String baseUrl = server.getHealthCheckUrl();

        // healthCheckUrl이 이미 전체 URL인 경우 (http:// 또는 https://로 시작)
        if (baseUrl != null && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
            return baseUrl;
        }

        // healthCheckUrl이 도메인만 있는 경우
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return "https://" + baseUrl + healthCheckPath;
        }

        // healthCheckUrl이 없는 경우 (오류)
        log.error("헬스 체크 URL이 설정되지 않음: serverId={}", server.getId());
        return "";
    }

    /**
     * 서버를 헬스 체크 스케줄러에 등록
     * isHealthCheckActive 플래그가 true이고 healthCheckUrl이 있을 때만 등록
     *
     * @param server 도메인 서버
     */
    public void registerHealthCheck(DomainServer server) {
        // isHealthCheckActive 플래그가 false면 등록하지 않음
        if (!server.getIsHealthCheckActive()) {
            log.debug("헬스 체크가 비활성화되어 등록하지 않음: serverId={}", server.getId());
            return;
        }

        if (server.getHealthCheckUrl() == null || server.getHealthCheckUrl().isEmpty()) {
            log.warn("헬스 체크 URL이 없어 등록하지 않음: serverId={}", server.getId());
            return;
        }

        String interval = server.getHealthCheckInterval();
        String healthCheckPath = extractPath(server.getHealthCheckUrl());

        redisHealthCheckCache.addHealthCheckJob(interval, server.getId(), healthCheckPath);

        log.info("헬스 체크 등록: serverId={}, interval={}, path={}", server.getId(), interval, healthCheckPath);
    }

    /**
     * 서버를 헬스 체크 스케줄러에서 제거
     *
     * @param server 도메인 서버
     */
    public void unregisterHealthCheck(DomainServer server) {
        String interval = server.getHealthCheckInterval();
        String healthCheckPath = extractPath(server.getHealthCheckUrl());

        redisHealthCheckCache.removeHealthCheckJob(interval, server.getId(), healthCheckPath);

        // 실패 카운트도 삭제
        resetFailureCount(server.getId());

        log.info("헬스 체크 제거: serverId={}, interval={}", server.getId(), interval);
    }

    /**
     * URL에서 경로 추출
     *
     * @param url 전체 URL
     * @return 경로 부분 (예: /api/health)
     */
    private String extractPath(String url) {
        if (url == null || url.isEmpty()) {
            return "/health"; // 기본값
        }

        // 이미 경로만 있는 경우
        if (url.startsWith("/")) {
            return url;
        }

        // 전체 URL인 경우 경로 추출
        try {
            String[] parts = url.split("//");
            if (parts.length > 1) {
                String remainder = parts[1];
                int pathStart = remainder.indexOf("/");
                if (pathStart >= 0) {
                    return remainder.substring(pathStart);
                }
            }
        } catch (Exception e) {
            log.warn("URL 경로 추출 실패: url={}", url);
        }

        return "/health"; // 기본값
    }

    /**
     * 헬스 체크 실패 알림 생성
     */
    private void createHealthCheckFailureNotification(DomainServer server, int failureCount) {
        try {
            // metadata 생성
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("serverId", server.getId());
            metadata.put("serverName", server.getName());
            metadata.put("projectId", server.getProjectId());
            metadata.put("failureCount", failureCount);
            metadata.put("healthCheckUrl", server.getHealthCheckUrl());
            String metadataJson = objectMapper.writeValueAsString(metadata);

            // 알림 생성
            notificationService.createNotification(
                    null,  // recipientUserId (null, 서버 귀속)
                    server.getId(),  // domainServerId
                    NotificationType.HEALTH_CHECK_FAILURE,
                    "헬스 체크 실패",
                    String.format("서버 '%s'의 헬스 체크가 실패했습니다. (%d회 연속 실패)",
                            server.getName(), failureCount),
                    metadataJson
            );
            log.info("헬스 체크 실패 알림 생성: serverId={}, failureCount={}", server.getId(), failureCount);
        } catch (Exception e) {
            log.error("헬스 체크 실패 알림 생성 실패: serverId={}, error={}", server.getId(), e.getMessage());
        }
    }

    /**
     * 서버 상태 변경 알림 생성
     */
    private void createServerStatusChangeNotification(DomainServer server, ServerStatus from, ServerStatus to) {
        try {
            // metadata 생성
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("serverId", server.getId());
            metadata.put("serverName", server.getName());
            metadata.put("projectId", server.getProjectId());
            metadata.put("fromStatus", from.name());
            metadata.put("toStatus", to.name());
            String metadataJson = objectMapper.writeValueAsString(metadata);

            // 알림 메시지 생성
            String message;
            if (to == ServerStatus.ERROR) {
                message = String.format("서버 '%s'가 ERROR 상태로 전환되었습니다. (이전 상태: %s)",
                        server.getName(), from.name());
            } else if (to == ServerStatus.DEPLOYED && from == ServerStatus.ERROR) {
                message = String.format("서버 '%s'가 정상 상태로 복구되었습니다.",
                        server.getName());
            } else {
                message = String.format("서버 '%s'의 상태가 변경되었습니다. (%s → %s)",
                        server.getName(), from.name(), to.name());
            }

            // 알림 생성
            notificationService.createNotification(
                    null,  // recipientUserId (null, 서버 귀속)
                    server.getId(),  // domainServerId
                    NotificationType.SERVER_STATUS_CHANGED,
                    "서버 상태 변경",
                    message,
                    metadataJson
            );
            log.info("서버 상태 변경 알림 생성: serverId={}, {} → {}", server.getId(), from, to);
        } catch (Exception e) {
            log.error("서버 상태 변경 알림 생성 실패: serverId={}, error={}", server.getId(), e.getMessage());
        }
    }
}