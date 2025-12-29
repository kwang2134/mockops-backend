package com.mockops.presentation.api.project.dto.projectmember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectNicknameRequest(
        @NotBlank(message = "프로젝트 닉네임은 필수입니다")
        @Size(min = 1, max = 50, message = "프로젝트 닉네임은 1자 이상 50자 이하여야 합니다")
        String projectNickname
) {
}
