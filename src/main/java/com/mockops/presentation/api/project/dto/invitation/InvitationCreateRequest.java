package com.mockops.presentation.api.project.dto.invitation;

import com.mockops.domain.project.role.MemberRole;

/**
 * 팀원 초대 요청 DTO
 */
public record InvitationCreateRequest(
    String email,
    MemberRole memberRole
) {
}
