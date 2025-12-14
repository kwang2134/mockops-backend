package com.mockops.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.global.config.LegalProperties;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 약관 동의 강제 검증 필터
 * JWT Access Token의 Claim에서 약관 동의 상태를 확인하고, 미동의 시 403 Forbidden 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsentEnforcementFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final LegalProperties legalProperties;
    private final ObjectMapper objectMapper;

    /**
     * 약관 동의 검증이 필요 없는 경로들
     */
    private static final String[] EXCLUDED_PATHS = {
            "/api/v1/auth/",
            "/login/oauth2/",
            "/public/",
            "/api/v1/users/me/agreements",  // 약관 동의 API는 제외
            "/swagger-ui/",
            "/v3/api-docs/",
            "/docs/",
            "/api/v1/test/",
            "/mock/",
            "/api/webhook/"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 제외 경로 확인
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청은 통과 (이미 JwtAuthenticationFilter에서 처리됨)
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = (Long) authentication.getPrincipal();

            // Authorization 헤더에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);
            if (!StringUtils.hasText(token)) {
                log.warn("JWT 토큰이 없습니다: userId={}", userId);
                sendConsentRequiredResponse(request, response);
                return;
            }

            // JWT Claim에서 약관 동의 버전 추출
            String tosAgreedVersion = jwtProvider.getTosAgreedVersionFromToken(token);
            String ppAgreedVersion = jwtProvider.getPpAgreedVersionFromToken(token);

            // 최신 버전과 비교
            boolean hasValidTos = legalProperties.getTosVersion().equals(tosAgreedVersion);
            boolean hasValidPp = legalProperties.getPpVersion().equals(ppAgreedVersion);

            if (!hasValidTos || !hasValidPp) {
                log.warn("약관 미동의 사용자 접근 차단: userId={}, tosAgreed={}, ppAgreed={}, requiredTos={}, requiredPp={}",
                        userId, tosAgreedVersion, ppAgreedVersion, legalProperties.getTosVersion(), legalProperties.getPpVersion());
                sendConsentRequiredResponse(request, response);
                return;
            }

            // 약관 동의 완료, 요청 진행
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("약관 동의 검증 중 오류 발생", e);
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Request에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 약관 미동의 에러 응답 전송
     */
    private void sendConsentRequiredResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.CONSENT_REQUIRED, request.getRequestURI());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
