package com.mockops.global.filter;

import com.mockops.domain.project.service.ProjectCorsOriginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 동적 CORS 필터
 * Mock API 요청에 대해 프로젝트별로 등록된 Origin을 검증하여 동적으로 CORS를 허용
 * /mock/{projectId}/** 패턴의 요청에만 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicCorsFilter extends OncePerRequestFilter {

    private static final Pattern MOCK_API_PATTERN = Pattern.compile("^/mock/(\\d+)/.*");
    private static final String ORIGIN_HEADER = "Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    private final ProjectCorsOriginService projectCorsOriginService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String origin = request.getHeader(ORIGIN_HEADER);

        // Mock API 요청이 아니면 필터 통과
        Matcher matcher = MOCK_API_PATTERN.matcher(requestUri);
        if (!matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 프로젝트 ID 추출
        Long projectId = Long.parseLong(matcher.group(1));

        // Origin 헤더가 없으면 (같은 origin 요청) 통과
        if (origin == null || origin.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Origin 검증 (캐시 우선)
        boolean isAllowed = projectCorsOriginService.isOriginAllowed(projectId, origin);

        if (isAllowed) {
            // CORS 허용 헤더 설정
            response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, Content-Type, Accept, Authorization");
            response.setHeader(ACCESS_CONTROL_MAX_AGE, "3600");

            log.debug("CORS allowed for mock API request: projectId={}, origin={}", projectId, origin);

            // Preflight 요청(OPTIONS)이면 여기서 종료
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        } else {
            log.warn("CORS rejected for mock API request: projectId={}, origin={}", projectId, origin);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
