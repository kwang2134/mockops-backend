package com.mockops.presentation.api.project.dto.project;

import com.mockops.domain.project.entity.Project;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String ownerNickname,
        Integer unreadNotificationCount,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project, String ownerNickname, Integer unreadNotificationCount) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                ownerNickname,
                unreadNotificationCount,
                project.getUpdatedAt()
        );
    }
}
