package com.mockops.domain.project.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "project_cors_origins",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "origin_url"})
    }
)
public class ProjectCorsOrigin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false, length = 255, name = "origin_url")
    private String originUrl;

    @Builder
    public ProjectCorsOrigin(Long projectId, String originUrl) {
        this.projectId = projectId;
        this.originUrl = originUrl;
    }
}
