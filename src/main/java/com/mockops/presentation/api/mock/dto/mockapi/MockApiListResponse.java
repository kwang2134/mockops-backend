package com.mockops.presentation.api.mock.dto.mockapi;

import java.util.List;

/**
 * Mock API 목록 응답 DTO (커서 기반 페이징, 그룹핑)
 */
public record MockApiListResponse(
        List<MockApiGroupDto> mocks,
        Long nextCursorId,
        Boolean hasNext
) {
    public static MockApiListResponse of(List<MockApiGroupDto> groups, Long nextCursorId, Boolean hasNext) {
        return new MockApiListResponse(
                groups,
                nextCursorId,
                hasNext
        );
    }
}
