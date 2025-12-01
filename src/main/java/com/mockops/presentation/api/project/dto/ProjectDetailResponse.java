package com.mockops.presentation.api.project.dto;

import com.mockops.domain.project.entity.Project;
import com.mockops.domain.user.entity.User;

import java.time.Instant;

public record ProjectDetailResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerNickname,
        String slackWebhookUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectDetailResponse from(Project project, User owner) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                owner.getNickname(),
                project.getSlackWebhookUrl(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
