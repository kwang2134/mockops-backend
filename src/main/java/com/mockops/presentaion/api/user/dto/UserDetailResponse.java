package com.mockops.presentaion.api.user.dto;

import com.mockops.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDetailResponse {
    private Long id;
    private String email;
    private String nickname;
    private String role;

    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
