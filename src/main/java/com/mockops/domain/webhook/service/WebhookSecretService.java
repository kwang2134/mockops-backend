package com.mockops.domain.webhook.service;

import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.webhook.entity.WebhookSecret;
import com.mockops.domain.webhook.repository.WebhookSecretRepository;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.util.CryptUtils;
import com.mockops.presentation.api.webhook.dto.WebhookSecretResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Webhook Secret 관리 서비스
 * Secret Key의 생성, 암호화, 복호화, 재발급, 비활성화 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookSecretService {

    private final WebhookSecretRepository webhookSecretRepository;
    private final ProjectMemberService projectMemberService;
    private final CryptUtils cryptUtils;

    /**
     * 프로젝트 ID로 WebhookSecret 조회 (복호화된 키 반환 X)
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     * @return WebhookSecret 엔티티
     */
    public WebhookSecret getWebhookSecretByProjectId(Long projectId, Long currentUserId) {
        // 권한 검증: OWNER만 조회 가능
        projectMemberService.validateOwner(projectId, currentUserId);

        return webhookSecretRepository.findByProjectId(projectId)
                .orElseThrow(() -> ErrorCode.WEBHOOK_SECRET_NOT_FOUND.domainException(
                        "프로젝트에 WebhookSecret이 존재하지 않습니다. projectId=" + projectId
                ));
    }

    /**
     * 프로젝트 ID로 WebhookSecret 조회 (내부용)
     */
    public WebhookSecret getWebhookSecretByProjectIdInternal(Long projectId) {
        return webhookSecretRepository.findByProjectId(projectId)
                .orElseThrow(() -> ErrorCode.WEBHOOK_SECRET_NOT_FOUND.domainException(
                        "프로젝트에 WebhookSecret이 존재하지 않습니다. projectId=" + projectId
                ));
    }

    /**
     * 프로젝트 생성 시 WebhookSecret 자동 생성
     *
     * @param projectId 프로젝트 ID
     * @return 생성된 WebhookSecret
     */
    @Transactional
    /**
     * WebhookSecret 생성 (평문 반환)
     * 프로젝트 생성 시 사용 - 평문 secret을 반환하여 사용자에게 제공
     */
    public WebhookSecretCreateResult createWebhookSecretWithRaw(Long projectId) {
        // 이미 존재하는지 확인
        if (webhookSecretRepository.existsByProjectId(projectId)) {
            throw ErrorCode.WEBHOOK_SECRET_ALREADY_EXISTS.domainException(
                    "이미 WebhookSecret이 존재합니다. projectId=" + projectId
            );
        }

        // UUID 생성 및 암호화
        String rawSecretKey = UUID.randomUUID().toString();
        String encryptedSecretKey = cryptUtils.encrypt(rawSecretKey);

        WebhookSecret webhookSecret = WebhookSecret.builder()
                .projectId(projectId)
                .secretKey(encryptedSecretKey)
                .isActive(true)
                .build();

        WebhookSecret saved = webhookSecretRepository.save(webhookSecret);

        log.info("WebhookSecret 생성 완료 (평문 포함): projectId={}, webhookSecretId={}", projectId, saved.getId());

        return new WebhookSecretCreateResult(saved, rawSecretKey);
    }

    /**
     * WebhookSecret 생성 결과 (평문 포함)
     */
    public record WebhookSecretCreateResult(WebhookSecret webhookSecret, String rawSecretKey) {}

    /**
     * WebhookSecret 생성 (내부용 - 평문 반환 안 함)
     */
    public WebhookSecret createWebhookSecret(Long projectId) {
        return createWebhookSecretWithRaw(projectId).webhookSecret();
    }

    /**
     * Secret Key 재발급 (기존 키 즉시 폐기)
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     * @return 새로운 Secret Key (복호화된 원본, 사용자에게 한 번만 노출)
     */
    @Transactional
    public String reissueSecretKey(Long projectId, Long currentUserId) {
        // 권한 검증: OWNER만 재발급 가능
        projectMemberService.validateOwner(projectId, currentUserId);

        WebhookSecret webhookSecret = getWebhookSecretByProjectIdInternal(projectId);

        // 새로운 UUID 생성 및 암호화
        String newRawSecretKey = UUID.randomUUID().toString();
        String newEncryptedSecretKey = cryptUtils.encrypt(newRawSecretKey);

        webhookSecret.reissueSecretKey(newEncryptedSecretKey);

        log.info("WebhookSecret 재발급 완료: projectId={}, webhookSecretId={}", projectId, webhookSecret.getId());

        // 복호화된 원본 키 반환 (사용자에게 한 번만 노출)
        return newRawSecretKey;
    }

    /**
     * Secret Key 비활성화
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     */
    @Transactional
    public void deactivateSecretKey(Long projectId, Long currentUserId) {
        // 권한 검증: OWNER만 비활성화 가능
        projectMemberService.validateOwner(projectId, currentUserId);

        WebhookSecret webhookSecret = getWebhookSecretByProjectIdInternal(projectId);
        webhookSecret.deactivate();

        log.info("WebhookSecret 비활성화 완료: projectId={}, webhookSecretId={}", projectId, webhookSecret.getId());
    }

    /**
     * Secret Key 복호화 (내부용, Webhook JWT 검증 시 사용)
     *
     * @param projectId 프로젝트 ID
     * @return 복호화된 Secret Key
     */
    public String decryptSecretKey(Long projectId) {
        WebhookSecret webhookSecret = getWebhookSecretByProjectIdInternal(projectId);

        if (!webhookSecret.getIsActive()) {
            throw ErrorCode.WEBHOOK_SECRET_INACTIVE.domainException(
                    "비활성화된 WebhookSecret입니다. projectId=" + projectId
            );
        }

        return cryptUtils.decrypt(webhookSecret.getSecretKey());
    }

    /**
     * Secret Key 정보 조회 - DTO 반환 (컨트롤러용)
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     * @return WebhookSecretResponse DTO
     */
    public WebhookSecretResponse getWebhookSecretResponse(Long projectId, Long currentUserId) {
        WebhookSecret webhookSecret = getWebhookSecretByProjectId(projectId, currentUserId);
        return WebhookSecretResponse.from(webhookSecret);
    }

    /**
     * Secret Key 재발급 - DTO 반환 (컨트롤러용)
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     * @return WebhookSecretResponse DTO (새로 발급된 Secret Key 포함)
     */
    @Transactional
    public WebhookSecretResponse reissueSecretKeyWithResponse(Long projectId, Long currentUserId) {
        String newSecretKey = reissueSecretKey(projectId, currentUserId);
        WebhookSecret webhookSecret = getWebhookSecretByProjectIdInternal(projectId);
        return WebhookSecretResponse.fromWithSecretKey(webhookSecret, newSecretKey);
    }

    /**
     * Secret Key 비활성화 - DTO 반환 (컨트롤러용)
     *
     * @param projectId 프로젝트 ID
     * @param currentUserId 현재 사용자 ID
     * @return WebhookSecretResponse DTO
     */
    @Transactional
    public WebhookSecretResponse deactivateSecretKeyWithResponse(Long projectId, Long currentUserId) {
        deactivateSecretKey(projectId, currentUserId);
        WebhookSecret webhookSecret = getWebhookSecretByProjectIdInternal(projectId);
        return WebhookSecretResponse.from(webhookSecret);
    }
}