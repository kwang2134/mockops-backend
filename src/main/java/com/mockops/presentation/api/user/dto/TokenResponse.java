package com.mockops.presentation.api.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
