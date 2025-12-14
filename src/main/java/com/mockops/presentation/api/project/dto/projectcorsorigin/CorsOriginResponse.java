package com.mockops.presentation.api.project.dto.projectcorsorigin;

import com.mockops.domain.project.entity.ProjectCorsOrigin;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record CorsOriginResponse(
        @Schema(description = "CORS Origin ID", example = "1")
        Long id,
        @Schema(description = "허용할 Origin URL", example = "https://example.com")
        String originUrl,
        @Schema(description = "CORS Origin 생성 일시", example = "2025-12-12T02:00:00Z")
        Instant createdAt
) {
    public static CorsOriginResponse from(ProjectCorsOrigin corsOrigin) {
        return new CorsOriginResponse(
                corsOrigin.getId(),
                corsOrigin.getOriginUrl(),
                corsOrigin.getCreatedAt()
        );
    }
}
