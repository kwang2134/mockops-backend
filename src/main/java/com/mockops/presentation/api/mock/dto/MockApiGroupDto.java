package com.mockops.presentation.api.mock.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Mock API 그룹 DTO
 * name 필드를 기준으로 그룹핑된 Mock API 목록
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockApiGroupDto {

    private String groupName;
    private List<MockApiResponse> mocks;

    public static MockApiGroupDto of(String groupName, List<MockApiResponse> mocks) {
        return MockApiGroupDto.builder()
            .groupName(groupName)
            .mocks(mocks)
            .build();
    }
}
