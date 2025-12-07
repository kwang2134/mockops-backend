package com.mockops.global.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 비활성화된 Mock API 요청을 처리하기 위한 핸들러 객체
 * 503 Service Unavailable 응답을 반환합니다.
 */
@Getter
@AllArgsConstructor
public class InactiveMockApiHandler {

    private final Long projectId;
    private final String serverName;
    private final String requestedPath;
    private final Long mockApiId;
}