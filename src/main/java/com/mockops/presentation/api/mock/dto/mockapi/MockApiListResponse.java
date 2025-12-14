package com.mockops.presentation.api.mock.dto.mockapi;

import java.util.List;

/**
 * Mock API 목록 응답 DTO (복합 커서 페이징, name 정렬, 그룹핑)
 */
public record MockApiListResponse(
        List<MockApiGroupDto> mocks,
        String nextNameCursor,
        Long nextIdCursor,
        Boolean hasNext
) {
    public static MockApiListResponse of(List<MockApiGroupDto> groups, String nextNameCursor, Long nextIdCursor, Boolean hasNext) {
        return new MockApiListResponse(
                groups,
                nextNameCursor,
                nextIdCursor,
                hasNext
        );
    }
}
