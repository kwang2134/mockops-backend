package com.mockops.presentation.api.mock.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Mock API 목록 응답 DTO (커서 기반 페이징, 그룹핑)
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiListResponse {

    private List<MockApiGroupDto> mocks;
    private Long nextCursorId;
    private Boolean hasNext;

    public static MockApiListResponse of(List<MockApiGroupDto> groups, Long nextCursorId, Boolean hasNext) {
        return MockApiListResponse.builder()
            .mocks(groups)
            .nextCursorId(nextCursorId)
            .hasNext(hasNext)
            .build();
    }
}
