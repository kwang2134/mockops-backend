package com.mockops.presentation.api.user.dto;

import com.mockops.domain.user.entity.User;

public record UserDetailResponse(
        Long id,
        String email,
        String nickname,
        String role
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
