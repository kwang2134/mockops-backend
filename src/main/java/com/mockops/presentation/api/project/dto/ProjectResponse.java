package com.mockops.presentation.api.project.dto;

import com.mockops.domain.project.entity.Project;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String ownerNickname
) {
    public static ProjectResponse from(Project project, String ownerNickname) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                ownerNickname
        );
    }
}
