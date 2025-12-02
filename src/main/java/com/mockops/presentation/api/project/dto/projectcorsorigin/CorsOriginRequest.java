package com.mockops.presentation.api.project.dto.projectcorsorigin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CorsOriginRequest(
        @NotBlank(message = "Origin URL은 필수입니다")
        @Size(max = 255, message = "Origin URL은 255자 이하여야 합니다")
        String originUrl
) {
}
