package com.mockops.presentation.api.notification.dto;

import java.util.List;

/**
 * 헬스 체크 실패 로그 페이지 응답 DTO
 */
public record HealthCheckFailureLogPageResponse(
        List<HealthCheckFailureLogDto> failures,
        Long nextCursorId,
        Boolean hasNext
) {
    public static HealthCheckFailureLogPageResponse of(
            List<HealthCheckFailureLogDto> failures,
            Long nextCursorId,
            Boolean hasNext
    ) {
        return new HealthCheckFailureLogPageResponse(
                failures,
                nextCursorId,
                hasNext
        );
    }
}
