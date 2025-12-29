package com.mockops.presentation.api.webhook;

import com.mockops.domain.mock.service.DomainServerService;
import com.mockops.domain.webhook.service.DeploymentService;
import com.mockops.domain.webhook.service.WebhookAuthService;
import com.mockops.presentation.api.webhook.docs.WebhookDocs;
import com.mockops.presentation.api.webhook.dto.DeploymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook 수신 API
 * CI/CD 배포 완료 이벤트를 수신하고 비동기 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController implements WebhookDocs {

    private final WebhookAuthService webhookAuthService;
    private final DeploymentService deploymentEventService;
    private final DomainServerService domainServerService;

    /**
     * CI/CD 배포 완료 WebHook 수신
     * POST /api/webhook/deploy/{projectId}
     *
     * @param projectId 프로젝트 ID (URL 경로)
     * @param authHeader Authorization 헤더 (Bearer {Webhook JWT})
     * @param request 배포 이벤트 정보
     * @return 202 Accepted (비동기 처리)
     */
    @Override
    @PostMapping("/deploy/{projectId}")
    public ResponseEntity<Void> receiveDeploymentEvent(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeploymentRequest request
    ) {
        log.info("Webhook 수신: projectId={}, projectName={}, domainServerName={}",
                projectId, request.projectName(), request.domainServerName());

        // 1. Webhook 인증 및 검증
        if (!webhookAuthService.validateWebhookRequest(authHeader, projectId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 서버 상태 변경
        domainServerService.prepareForDeployment(projectId, request);

        // 3. 헬스체크 Redis 등록
        deploymentEventService.registerDeploymentJob(projectId, request);

        // 3. 202 Accepted 즉시 반환 (비동기 처리)
        return ResponseEntity.accepted().build();
    }
}