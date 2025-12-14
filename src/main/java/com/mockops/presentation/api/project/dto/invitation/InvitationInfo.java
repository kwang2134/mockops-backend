package com.mockops.presentation.api.project.dto.invitation;

import com.mockops.domain.project.entity.Invitation;
import com.mockops.domain.project.role.MemberRole;

import java.time.Instant;

/**
 * 초대 정보 DTO (목록용)
 */
public record InvitationInfo(
    Long invitationId,
    String invitedEmail,
    Instant expiresAt,
    MemberRole memberRole
) {
    public static InvitationInfo from(Invitation invitation) {
        return new InvitationInfo(
            invitation.getId(),
            invitation.getInvitedEmail(),
            invitation.getExpiresAt(),
            invitation.getMemberRole()
        );
    }
}
