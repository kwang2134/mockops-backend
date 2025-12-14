package com.mockops.presentation.api.project.dto.project;

import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.user.entity.User;

import java.time.Instant;

public record ProjectDetailResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerNickname,
        String slackWebhookUrl,
        MemberRole currentUserMemberRole,  // 현재 로그인된 유저의 프로젝트 멤버 권한
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectDetailResponse from(Project project, User owner, MemberRole currentUserMemberRole) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerId(),
                owner.getNickname(),
                project.getSlackWebhookUrl(),
                currentUserMemberRole,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
