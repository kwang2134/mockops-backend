package com.mockops.presentation.api.project.dto.projectmember;

import com.mockops.domain.project.role.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull(message = "멤버 역할은 필수입니다")
        MemberRole memberRole
) {
}
