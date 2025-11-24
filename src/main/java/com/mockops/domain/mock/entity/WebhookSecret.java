package com.mockops.domain.mock.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "webhook_secrets")
public class WebhookSecret extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String secretKey;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public WebhookSecret(Long projectId, String secretKey, Boolean isActive) {
        this.projectId = projectId;
        this.secretKey = secretKey;
        this.isActive = isActive;
    }
}
