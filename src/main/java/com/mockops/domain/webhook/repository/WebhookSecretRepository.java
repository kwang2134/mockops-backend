package com.mockops.domain.webhook.repository;

import com.mockops.domain.webhook.entity.WebhookSecret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookSecretRepository extends JpaRepository<WebhookSecret, Long> {

    /**
     * 프로젝트 ID로 WebhookSecret 조회
     */
    Optional<WebhookSecret> findByProjectId(Long projectId);

    /**
     * 프로젝트 ID로 WebhookSecret 존재 여부 확인
     */
    boolean existsByProjectId(Long projectId);
}