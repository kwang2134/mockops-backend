package com.mockops.presentation.api.project.dto.projectcorsorigin;

import com.mockops.domain.project.entity.ProjectCorsOrigin;

public record CorsOriginResponse(
        Long id,
        String originUrl
) {
    public static CorsOriginResponse from(ProjectCorsOrigin corsOrigin) {
        return new CorsOriginResponse(
                corsOrigin.getId(),
                corsOrigin.getOriginUrl()
        );
    }
}
