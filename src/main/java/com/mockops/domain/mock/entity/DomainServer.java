package com.mockops.domain.mock.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "domain_servers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_project_slug", columnNames = {"project_id", "slug"})
})
public class DomainServer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status;

    @Column
    private String healthCheckUrl;

    @Column(nullable = false, length = 3, columnDefinition = "VARCHAR(3) DEFAULT '10m'")
    private String healthCheckInterval;

    @Column
    private Instant lastCheckedAt;

    @Column(nullable = false)
    private Boolean isHealthCheckActive;

    @Builder
    public DomainServer(Long projectId, String name, String slug, ServerStatus status, String healthCheckUrl,
                       String healthCheckInterval, Boolean isHealthCheckActive) {
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.status = status != null ? status : ServerStatus.MOCKING;
        this.healthCheckUrl = healthCheckUrl;
        this.healthCheckInterval = healthCheckInterval != null ? healthCheckInterval : "10m";
        this.isHealthCheckActive = isHealthCheckActive != null ? isHealthCheckActive : false;
    }

    /**
     * 서버 정보 및 상태 수정
     */
    public void updateServerInfo(String name, String healthCheckUrl,
                                 String healthCheckInterval, ServerStatus status,
                                 Boolean isHealthCheckActive) {
        if (name != null) {
            this.name = name;
        }
        if (healthCheckUrl != null) {
            this.healthCheckUrl = healthCheckUrl;
        }
        if (healthCheckInterval != null) {
            this.healthCheckInterval = healthCheckInterval;
        }
        if (status != null) {
            this.status = status;
        }
        if (isHealthCheckActive != null) {
            this.isHealthCheckActive = isHealthCheckActive;
        }
    }

    /**
     * 헬스 체크 활성화 상태 변경
     */
    public void updateHealthCheckActive(Boolean isActive) {
        if (isActive != null) {
            this.isHealthCheckActive = isActive;
        }
    }

    /**
     * 헬스 체크 수행 시각 업데이트
     */
    public void updateLastCheckedAt() {
        this.lastCheckedAt = Instant.now();
    }

    /**
     * 서버 상태 변경
     */
    public void updateStatus(ServerStatus newStatus) {
        this.status = newStatus;
    }
}
