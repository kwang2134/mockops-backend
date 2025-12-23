package com.mockops.domain.healthcheck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.healthcheck.infrastructure.HealthCheckCachePort;
import com.mockops.domain.healthcheck.infrastructure.HealthCheckFailurePort;
import com.mockops.domain.healthcheck.util.HealthCheckJobParser;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.mock.repository.DomainServerRepository;
import com.mockops.domain.notification.entity.NotificationType;
import com.mockops.domain.notification.service.NotificationService;
import com.mockops.domain.notification.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 도메인 서버 헬스 체크 서비스
 * 스케줄러에서 호출하여 서버 상태를 확인하고 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private static final int FAILURE_THRESHOLD = 3; // 3회 연속 실패 시 ERROR 상태로 전환

    private final WebClient healthCheckWebClient;
    private final DomainServerRepository domainServerRepository;
    private final HealthCheckCachePort healthCheckCachePort;
    private final HealthCheckFailurePort healthCheckFailurePort;
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

        Set<String> jobs = healthCheckCachePort.getAllHealthCheckJobs(interval);

        if (jobs.isEmpty()) {
            log.debug("헬스 체크 대상 없음: interval={}", interval);
            return;
        }

        log.info("헬스 체크 대상: interval={}, count={}", interval, jobs.size());

        for (String job : jobs) {
            try {
                Long serverId = HealthCheckJobParser.parseServerId(job);
                String healthCheckPath = HealthCheckJobParser.parseHealthCheckPath(job);

                performHealthCheck(serverId, healthCheckPath, interval);
            } catch (Exception e) {
                log.error("헬스 체크 처리 중 오류 발생: job={}, error={}", job, e.getMessage(), e);
            }
        }

        log.info("헬스 체크 벌크 작업 완료: interval={}, processed={}", interval, jobs.size());
    }

    /**
     * 헬스 체크 결과를 담는 내부 레코드
     */
    private record HealthCheckResult(boolean isHealthy, String errorMessage, String errorType) {
        static HealthCheckResult success() {
            return new HealthCheckResult(true, null, null);
        }

        static HealthCheckResult failure(String errorMessage, String errorType) {
            return new HealthCheckResult(false, errorMessage, errorType);
        }
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
        HealthCheckResult result = checkHealth(fullUrl);

        // 결과에 따라 서버 상태 업데이트
        updateServerStatus(server, result, interval);

        // lastCheckedAt 업데이트
        server.updateLastCheckedAt();

        // 변경사항 명시적 저장
        domainServerRepository.save(server);

        log.info("헬스 체크 완료: serverId={}, url={}, isHealthy={}, status={}",
                serverId, fullUrl, result.isHealthy(), server.getStatus());
    }

    /**
     * WebClient를 사용한 헬스 체크 수행
     *
     * @param url 헬스 체크 URL
     * @return 헬스 체크 결과 (성공/실패, 에러 메시지, 에러 타입)
     */
    private HealthCheckResult checkHealth(String url) {
        try {
            // 에러 정보를 저장할 변수
            final String[] errorInfo = new String[2]; // [0]: errorMessage, [1]: errorType

            HttpStatusCode status = healthCheckWebClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode())
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> {
                        // 에러 타입과 메시지 추출
                        String errorType = extractErrorType(e);
                        String errorMessage = extractErrorMessage(e);

                        errorInfo[0] = errorMessage;
                        errorInfo[1] = errorType;

                        log.warn("헬스 체크 요청 실패: url={}, errorType={}, error={}", url, errorType, errorMessage);
                        return Mono.empty();
                    })
                    .block();

            if (status != null && status.is2xxSuccessful()) {
                log.debug("헬스 체크 성공: url={}, status={}", url, status);
                return HealthCheckResult.success();
            } else if (status != null) {
                // 2xx가 아닌 응답
                String errorMessage = String.format("HTTP %d 응답", status.value());
                log.debug("헬스 체크 실패: url={}, status={}", url, status);
                return HealthCheckResult.failure(errorMessage, "HTTP_ERROR");
            } else if (errorInfo[0] != null) {
                // onErrorResume에서 캡처한 에러
                return HealthCheckResult.failure(errorInfo[0], errorInfo[1]);
            } else {
                // 알 수 없는 실패
                return HealthCheckResult.failure("응답 없음", "NO_RESPONSE");
            }
        } catch (Exception e) {
            String errorType = extractErrorType(e);
            String errorMessage = extractErrorMessage(e);
            log.error("헬스 체크 중 예외 발생: url={}, errorType={}, error={}", url, errorType, errorMessage);
            return HealthCheckResult.failure(errorMessage, errorType);
        }
    }

    /**
     * 예외에서 상세 에러 메시지 추출 (metadata용)
     * 콜론(:) 뒤의 첫 번째 의미있는 메시지를 추출
     */
    private String extractErrorMessage(Throwable e) {
        String message = e.getMessage();

        if (message == null || message.isEmpty()) {
            return "알 수 없는 오류";
        }

        // 콜론(:) 뒤의 첫 번째 메시지 추출
        // 예: "PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed"
        // → "PKIX path validation failed"
        int firstColonIndex = message.indexOf(':');
        if (firstColonIndex >= 0 && firstColonIndex < message.length() - 1) {
            String afterFirstColon = message.substring(firstColonIndex + 1).trim();

            // 두 번째 콜론이 있으면 그 전까지만 추출
            int secondColonIndex = afterFirstColon.indexOf(':');
            if (secondColonIndex >= 0) {
                String extractedMessage = afterFirstColon.substring(0, secondColonIndex).trim();
                if (!extractedMessage.isEmpty()) {
                    return extractedMessage;
                }
            } else {
                // 두 번째 콜론이 없으면 첫 콜론 이후 전체를 반환 (최대 200자)
                if (afterFirstColon.length() > 200) {
                    return afterFirstColon.substring(0, 200) + "...";
                }
                return afterFirstColon;
            }
        }

        // 콜론이 없는 경우 원본 메시지 반환 (최대 200자)
        if (message.length() > 200) {
            return message.substring(0, 200) + "...";
        }

        return message;
    }

    /**
     * 예외에서 에러 타입 추출 (알림 제목용)
     * 예외 클래스 이름을 그대로 반환 (예: "SSLHandshakeException", "TimeoutException")
     */
    private String extractErrorType(Throwable e) {
        return e.getClass().getSimpleName();
    }

    /**
     * 헬스 체크 결과에 따라 서버 상태 업데이트
     *
     * @param server 도메인 서버
     * @param result 헬스 체크 결과
     * @param interval 헬스 체크 주기 (TTL 계산용)
     */
    private void updateServerStatus(DomainServer server, HealthCheckResult result, String interval) {
        ServerStatus previousStatus = server.getStatus();

        if (result.isHealthy()) {
            // 헬스 체크 성공 시
            handleHealthyServer(server, previousStatus);
        } else {
            // 헬스 체크 실패 시
            handleUnhealthyServer(server, previousStatus, interval, result);
        }
    }

    /**
     * 헬스 체크 성공 시 처리
     */
    private void handleHealthyServer(DomainServer server, ServerStatus previousStatus) {
        // 실패 카운트 초기화
        healthCheckFailurePort.resetFailureCount(server.getId());

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
     * @param result 헬스 체크 결과 (에러 정보 포함)
     */
    private void handleUnhealthyServer(DomainServer server, ServerStatus previousStatus, String interval, HealthCheckResult result) {
        // 실패 카운트 증가 (interval에 따라 적절한 TTL 설정)
        int failureCount = healthCheckFailurePort.incrementFailureCount(server.getId(), interval);

        log.warn("헬스 체크 실패: serverId={}, interval={}, failureCount={}/{}, error={}",
                server.getId(), interval, failureCount, FAILURE_THRESHOLD, result.errorMessage());

        // 헬스 체크 실패 알림 생성 (에러 정보 포함)
        createHealthCheckFailureNotification(server, failureCount, result);

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

        healthCheckCachePort.addHealthCheckJob(interval, server.getId(), healthCheckPath);

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

        healthCheckCachePort.removeHealthCheckJob(interval, server.getId(), healthCheckPath);

        // 실패 카운트도 삭제
        healthCheckFailurePort.resetFailureCount(server.getId());

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
     *
     * @param server 도메인 서버
     * @param failureCount 연속 실패 횟수
     * @param result 헬스 체크 결과 (에러 정보 포함)
     */
    private void createHealthCheckFailureNotification(DomainServer server, int failureCount, HealthCheckResult result) {
        try {
            // metadata 생성 (사용자에게 유용한 정보만 포함)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("healthCheckUrl", server.getHealthCheckUrl());
            metadata.put("serverName", server.getName());
            metadata.put("failureCount", failureCount);
            metadata.put("errorType", result.errorType());
            metadata.put("errorMessage", result.errorMessage());
            String metadataJson = objectMapper.writeValueAsString(metadata);

            // 알림 메시지에 에러 원인 포함
            String message = String.format("서버 '%s'의 헬스 체크가 실패했습니다. (%d회 연속 실패)\n원인: %s",
                    server.getName(), failureCount, result.errorMessage());

            // 알림 생성 (title은 예외 클래스 이름만 사용하여 100자 제한 준수)
            notificationService.createNotification(
                    null,  // recipientUserId (null, 서버 귀속)
                    server.getId(),  // domainServerId
                    NotificationType.HEALTH_CHECK_FAILURE,
                    result.errorType(),  // 예외 클래스 이름만 (예: "SSLHandshakeException")
                    message,
                    metadataJson
            );
            log.info("헬스 체크 실패 알림 생성: serverId={}, failureCount={}, error={}",
                    server.getId(), failureCount, result.errorMessage());
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