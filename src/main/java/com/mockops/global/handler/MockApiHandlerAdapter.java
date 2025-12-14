package com.mockops.global.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * MockApiHandler와 InactiveMockApiHandler를 처리하는 HandlerAdapter
 * Mock 응답 또는 비활성화 에러 응답을 클라이언트에게 반환합니다.
 */
@Slf4j
@Component
public class MockApiHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return handler instanceof MockApiHandler || handler instanceof InactiveMockApiHandler;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {

        // 비활성화된 Mock API 처리
        if (handler instanceof InactiveMockApiHandler) {
            return handleInactiveMockApi(response, (InactiveMockApiHandler) handler);
        }

        // 정상 Mock API 처리
        MockApiHandler mockApiHandler = (MockApiHandler) handler;

        log.info("Mock API 응답 반환: projectId={}, serverName={}, path={}, statusCode={}",
            mockApiHandler.getProjectId(),
            mockApiHandler.getServerName(),
            mockApiHandler.getRequestedPath(),
            mockApiHandler.getMockData().getStatusCode());

        // 응답 설정
        response.setStatus(mockApiHandler.getMockData().getStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 응답 본문 작성
        response.getWriter().write(mockApiHandler.getMockData().getResponseBody());
        response.getWriter().flush();

        // ModelAndView를 null로 반환하여 뷰 렌더링 없이 응답 완료
        return null;
    }

    /**
     * 비활성화된 Mock API 요청 처리
     * 503 Service Unavailable 응답 반환
     */
    private ModelAndView handleInactiveMockApi(HttpServletResponse response, InactiveMockApiHandler handler)
        throws IOException {

        log.warn("비활성화된 Mock API 응답 반환: projectId={}, serverName={}, path={}, mockApiId={}",
            handler.getProjectId(),
            handler.getServerName(),
            handler.getRequestedPath(),
            handler.getMockApiId());

        // 503 Service Unavailable 응답
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 에러 응답 본문 (UnifiedResponse 형식과 유사)
        String errorResponse = String.format(
            "{\"success\":false,\"message\":\"This Mock API is currently inactive. Please activate it in the MockOps dashboard.\",\"data\":null,\"mockApiId\":%d}",
            handler.getMockApiId()
        );

        response.getWriter().write(errorResponse);
        response.getWriter().flush();

        return null;
    }
}
