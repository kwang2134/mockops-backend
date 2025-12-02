package com.mockops.presentation.api.mock.dto.mockapi;

import java.util.List;

/**
 * Mock API 그룹 DTO
 * name 필드를 기준으로 그룹핑된 Mock API 목록
 */
public record MockApiGroupDto(
        String groupName,
        List<MockApiResponse> mocks
) {
    public static MockApiGroupDto of(String groupName, List<MockApiResponse> mocks) {
        return new MockApiGroupDto(
                groupName,
                mocks
        );
    }
}
