package com.mockops.presentation.api.project.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 멤버 초대 수락 요청 DTO
 * 웹 서비스를 통해 알림으로 받은 초대 수락을 위한 요청
 */
public record MemberInviteAcceptRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Long projectId
) {
}
