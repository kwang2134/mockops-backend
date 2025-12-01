package com.mockops.domain.webhook.service;

import com.mockops.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Webhook 인증 서비스
 * Webhook 요청의 JWT 토큰을 검증하고 프로젝트 ID를 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookAuthService {

    private final JwtProvider jwtProvider;
    private final WebhookSecretService webhookSecretService;

    /**
     * Webhook 인증 및 검증
     * 1. Authorization 헤더에서 JWT 추출
     * 2. Webhook Secret 조회 및 복호화
     * 3. JWT 검증 (Secret Key 사용)
     * 4. JWT의 projectId와 요청 경로의 projectId 비교
     *
     * @param authHeader Authorization 헤더
     * @param projectId 요청 경로의 프로젝트 ID
     * @return 인증 성공 여부
     */
    public boolean validateWebhookRequest(String authHeader, Long projectId) {
        // 1. JWT 추출
        String token = extractTokenFromHeader(authHeader);
        if (!StringUtils.hasText(token)) {
            log.error("Authorization 헤더에 유효한 토큰이 없습니다.");
            return false;
        }

        // 2. Webhook Secret 조회 (복호화)
        String secretKey;
        try {
            secretKey = webhookSecretService.decryptSecretKey(projectId);
        } catch (Exception e) {
            log.error("Webhook Secret 조회 실패: projectId={}, error={}", projectId, e.getMessage());
            return false;
        }

        // 3. JWT 검증 (프로젝트별 Secret Key 사용)
        if (!jwtProvider.validateWebhookToken(token, secretKey)) {
            log.error("Webhook JWT 검증 실패: projectId={}", projectId);
            return false;
        }

        // 4. JWT에서 projectId 추출 및 비교
        Long tokenProjectId;
        try {
            tokenProjectId = jwtProvider.getProjectIdFromWebhookToken(token, secretKey);
        } catch (Exception e) {
            log.error("JWT에서 projectId 추출 실패: error={}", e.getMessage());
            return false;
        }

        if (!tokenProjectId.equals(projectId)) {
            log.error("JWT의 projectId와 요청 경로의 projectId가 일치하지 않습니다. token={}, path={}",
                tokenProjectId, projectId);
            return false;
        }

        log.info("Webhook JWT 검증 성공: projectId={}", projectId);
        return true;
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}