package com.mockops.global.handler;

import com.mockops.domain.mock.dto.MockCacheDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mock API 요청을 처리하기 위한 핸들러 객체
 * HandlerMapping에서 반환되어 HandlerAdapter에 전달됩니다.
 */
@Getter
@AllArgsConstructor
public class MockApiHandler {

    private final Long projectId;
    private final String serverName;
    private final String requestedPath;
    private final MockCacheDto mockData;
}
