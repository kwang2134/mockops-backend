package com.mockops.presentation.api.webhook;

import com.mockops.domain.webhook.service.WebhookSecretService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.global.security.JwtProperties;
import com.mockops.global.security.JwtProvider;
import com.mockops.presentation.api.webhook.dto.WebhookSecretResponse;
import com.mockops.presentation.api.webhook.dto.WebhookTokenIssueRequest;
import com.mockops.presentation.api.webhook.dto.WebhookTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook Secret 관리 API
 * PROJECT_OWNER 권한 필요
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/webhook")
@RequiredArgsConstructor
public class WebhookSecretController {

    private final WebhookSecretService webhookSecretService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    /**
     * Secret Key 정보 조회 (키 값은 숨김)
     * GET /api/v1/projects/{projectId}/webhook/secret
     */
    @GetMapping("/secret")
    public ResponseEntity<UnifiedResponse<WebhookSecretResponse>> getSecretInfo(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("WebhookSecret 정보 조회: projectId={}, userId={}", projectId, userId);

        WebhookSecretResponse response = webhookSecretService.getWebhookSecretResponse(projectId, userId);

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * Secret Key 재발급 (기존 키 즉시 폐기)
     * POST /api/v1/projects/{projectId}/webhook/reissue
     */
    @PostMapping("/reissue")
    public ResponseEntity<UnifiedResponse<WebhookSecretResponse>> reissueSecretKey(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("WebhookSecret 재발급 요청: projectId={}, userId={}", projectId, userId);

        WebhookSecretResponse response = webhookSecretService.reissueSecretKeyWithResponse(projectId, userId);

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * Secret Key 비활성화 (isActive=false)
     * PATCH /api/v1/projects/{projectId}/webhook/deactivate
     */
    @PatchMapping("/deactivate")
    public ResponseEntity<UnifiedResponse<WebhookSecretResponse>> deactivateSecretKey(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("WebhookSecret 비활성화 요청: projectId={}, userId={}", projectId, userId);

        WebhookSecretResponse response = webhookSecretService.deactivateSecretKeyWithResponse(projectId, userId);

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }

    /**
     * 30일 유효 기간 Webhook JWT 발급
     * POST /api/v1/projects/{projectId}/webhook/token
     */
    @PostMapping("/token")
    public ResponseEntity<UnifiedResponse<WebhookTokenResponse>> issueWebhookToken(
            @PathVariable Long projectId,
            @Valid @RequestBody WebhookTokenIssueRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("Webhook JWT 발급 요청: projectId={}, userId={}", projectId, userId);

        // WebhookSecret 조회 및 권한 검증
        webhookSecretService.getWebhookSecretByProjectId(projectId, userId);

        // 사용자가 제공한 Secret Key를 사용하여 JWT 발급
        String jwtToken = jwtProvider.generateWebhookToken(projectId, request.secretKey());

        // 만료 시간 (초 단위)
        Long expiresInSeconds = jwtProperties.getWebhookTokenExpiration().toSeconds();

        WebhookTokenResponse response = WebhookTokenResponse.of(jwtToken, expiresInSeconds);

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }
}