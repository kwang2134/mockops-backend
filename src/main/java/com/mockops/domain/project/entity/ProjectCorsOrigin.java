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
@Table(name = "project_cors_origins")
public class ProjectCorsOrigin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String originUrl;

    @Builder
    public ProjectCorsOrigin(Long projectId, String originUrl) {
        this.projectId = projectId;
        this.originUrl = originUrl;
    }
}
