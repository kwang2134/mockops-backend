package com.mockops.presentation.api.project.dto;

import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.role.MemberRole;

public record ProjectMemberResponse(
        Long id,
        Long userId,
        String nickname,
        MemberRole memberRole
) {
    public static ProjectMemberResponse from(ProjectMember member, String nickname) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getUserId(),
                nickname,
                member.getMemberRole()
        );
    }
}
