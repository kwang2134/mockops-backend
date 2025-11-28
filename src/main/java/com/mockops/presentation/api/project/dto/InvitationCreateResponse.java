package com.mockops.presentation.api.project.dto;

import com.mockops.domain.project.entity.Invitation;

/**
 * 초대 생성 성공 응답 DTO
 */
public record InvitationCreateResponse(
    Long invitationId,
    String invitedEmail
) {
    public static InvitationCreateResponse from(Invitation invitation) {
        return new InvitationCreateResponse(
            invitation.getId(),
            invitation.getInvitedEmail()
        );
    }
}
