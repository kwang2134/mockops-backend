package com.mockops.presentation.api.project.dto.project;

import com.mockops.domain.project.entity.Project;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 프로젝트 생성 성공 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectCreateResponse {

    private Long id;
    private String name;
    private Instant createdAt;

    public static ProjectCreateResponse from(Project project) {
        return ProjectCreateResponse.builder()
            .id(project.getId())
            .name(project.getName())
            .createdAt(project.getCreatedAt())
            .build();
    }
}
