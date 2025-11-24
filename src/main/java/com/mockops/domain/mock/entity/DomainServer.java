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
@Table(name = "domain_servers")
public class DomainServer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerStatus status;

    @Column
    private String healthCheckUrl;

    @Column(nullable = false, length = 3, columnDefinition = "VARCHAR(3) DEFAULT '10m'")
    private String healthCheckInterval;

    @Column
    private Instant lastCheckedAt;

    @Builder
    public DomainServer(Long projectId, String name, ServerStatus status, String healthCheckUrl, String healthCheckInterval) {
        this.projectId = projectId;
        this.name = name;
        this.status = status;
        this.healthCheckUrl = healthCheckUrl;
        this.healthCheckInterval = healthCheckInterval != null ? healthCheckInterval : "10m";
    }
}
