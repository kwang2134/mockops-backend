package com.mockops.domain.notification.service;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Slack 알림 서비스
 * 도메인 서버 상태 변경 시 Slack 메시지 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final WebClient healthCheckWebClient;
    private final ProjectRepository projectRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"));

    /**
     * 서버 상태 변경 알림 전송
     *
     * @param server 도메인 서버
     * @param previousStatus 이전 상태
     * @param newStatus 새로운 상태
     */
    public void sendStatusChangeNotification(DomainServer server, ServerStatus previousStatus, ServerStatus newStatus) {
        // 프로젝트 조회
        Project project = projectRepository.findById(server.getProjectId()).orElse(null);

        if (project == null) {
            log.warn("프로젝트를 찾을 수 없어 Slack 알림을 전송하지 않음: projectId={}", server.getProjectId());
            return;
        }

        // Slack Webhook URL 확인
        if (project.getSlackWebhookUrl() == null || project.getSlackWebhookUrl().isEmpty()) {
            log.debug("Slack Webhook URL이 설정되지 않아 알림을 전송하지 않음: projectId={}", project.getId());
            return;
        }

        // 상태 변경 타입 확인 (DEPLOYED ↔ ERROR만 알림)
        if (!shouldNotify(previousStatus, newStatus)) {
            log.debug("알림 대상이 아닌 상태 변경: {} -> {}", previousStatus, newStatus);
            return;
        }

        // Slack 메시지 전송
        sendSlackMessage(project, server, previousStatus, newStatus);
    }

    /**
     * 알림 대상 상태 변경인지 확인
     */
    private boolean shouldNotify(ServerStatus previousStatus, ServerStatus newStatus) {
        // DEPLOYED -> ERROR (장애 발생)
        if (previousStatus == ServerStatus.DEPLOYED && newStatus == ServerStatus.ERROR) {
            return true;
        }

        // ERROR -> DEPLOYED (장애 복구)
        if (previousStatus == ServerStatus.ERROR && newStatus == ServerStatus.DEPLOYED) {
            return true;
        }

        return false;
    }

    /**
     * Slack 메시지 전송
     */
    private void sendSlackMessage(Project project, DomainServer server,
                                   ServerStatus previousStatus, ServerStatus newStatus) {
        String webhookUrl = project.getSlackWebhookUrl();
        String message = buildMessage(project, server, previousStatus, newStatus);

        try {
            // Slack Webhook 요청 페이로드
            Map<String, Object> payload = Map.of(
                    "text", message,
                    "username", "MockOps Health Check Bot",
                    "icon_emoji", getStatusEmoji(newStatus)
            );

            // 비동기로 Slack 메시지 전송
            healthCheckWebClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("Slack 알림 전송 성공: projectId={}, serverId={}",
                            project.getId(), server.getId()))
                    .doOnError(error -> log.error("Slack 알림 전송 실패: projectId={}, serverId={}, error={}",
                            project.getId(), server.getId(), error.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {
            log.error("Slack 메시지 전송 중 예외 발생: projectId={}, serverId={}, error={}",
                    project.getId(), server.getId(), e.getMessage(), e);
        }
    }

    /**
     * Slack 메시지 본문 구성
     */
    private String buildMessage(Project project, DomainServer server,
                                 ServerStatus previousStatus, ServerStatus newStatus) {
        String statusChangeType = (newStatus == ServerStatus.ERROR) ? "장애 발생" : "장애 복구";
        String timestamp = FORMATTER.format(Instant.now());

        return String.format(
                "*[%s] 서버 상태 변경*\n\n" +
                        "• 프로젝트: `%s`\n" +
                        "• 서버 이름: `%s`\n" +
                        "• 상태 변경: `%s` → `%s`\n" +
                        "• 변경 일시: %s\n" +
                        "• 헬스 체크 URL: <%s>",
                statusChangeType,
                project.getName(),
                server.getName(),
                previousStatus.name(),
                newStatus.name(),
                timestamp,
                server.getHealthCheckUrl()
        );
    }

    /**
     * 상태별 이모지 반환
     */
    private String getStatusEmoji(ServerStatus status) {
        return switch (status) {
            case DEPLOYED -> ":white_check_mark:";
            case ERROR -> ":x:";
            case MOCKING -> ":construction:";
            case PENDING -> ":hourglass_flowing_sand:";
        };
    }
}