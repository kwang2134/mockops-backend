package com.mockops.presentation.api.project.dto;

import com.mockops.domain.project.entity.Project;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 프로젝트 정보 수정 성공 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectUpdateResponse {

    private Long id;
    private Instant updatedAt;

    public static ProjectUpdateResponse from(Project project) {
        return ProjectUpdateResponse.builder()
            .id(project.getId())
            .updatedAt(project.getUpdatedAt())
            .build();
    }
}
