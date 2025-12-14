package com.mockops.domain.webhook.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로젝트별 CI/CD WebHook 요청의 JWT 서명 및 검증에 사용되는 고유 비밀 키를 저장
 */
@Entity
@Table(name = "webhook_secrets",
        uniqueConstraints = @UniqueConstraint(columnNames = "project_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookSecret extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 프로젝트 ID (FK 역할, Unique)
     */
    @Column(nullable = false, unique = true, name = "project_id")
    private Long projectId;

    /**
     * JWT 서명/검증에 사용되는 비밀 키 (AES-256 암호화 저장)
     * UUID 형태의 난수로 생성
     */
    @Column(nullable = false)
    private String secretKey;

    /**
     * 키 활성화 여부
     * false 시 모든 JWT 토큰 즉시 폐기
     */
    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public WebhookSecret(Long projectId, String secretKey, Boolean isActive) {
        this.projectId = projectId;
        this.secretKey = secretKey;
        this.isActive = isActive != null ? isActive : true;
    }

    /**
     * Secret Key 재발급 (새로운 암호화된 키로 교체)
     */
    public void reissueSecretKey(String newEncryptedSecretKey) {
        this.secretKey = newEncryptedSecretKey;
        this.isActive = true;
    }

    /**
     * Secret Key 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Secret Key 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}