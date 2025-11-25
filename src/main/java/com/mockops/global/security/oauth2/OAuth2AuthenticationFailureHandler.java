package com.mockops.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 실패 시 처리 핸들러
 * 프론트엔드로 에러 정보와 함께 리다이렉트
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // TODO: 프론트엔드 에러 리다이렉트 URL을 application.yml에 설정
    @Value("${oauth2.error-redirect-url:http://localhost:3000/oauth2/error}")
    private String errorRedirectUrl;

    /**
     * OAuth2 인증 실패 시 호출됨
     * 에러 메시지를 쿼리 파라미터로 전달하여 프론트엔드로 리다이렉트
     *
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     * @param exception 인증 예외
     * @throws IOException      I/O 에러
     * @throws ServletException Servlet 에러
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = exception.getLocalizedMessage();
        log.error("OAuth2 인증 실패: {}", errorMessage, exception);

        // 프론트엔드로 리다이렉트 (에러 메시지를 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(errorRedirectUrl)
                .queryParam("error", errorMessage != null ? errorMessage : "OAuth2 인증에 실패했습니다.")
                .build()
                .toUriString();

        log.info("OAuth2 인증 실패, 리다이렉트: targetUrl={}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
