package com.mockops.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Cookie 처리 유틸리티
 * HttpOnly, Secure 플래그를 적용한 안전한 쿠키 관리
 */
@Slf4j
@Component
public class CookieUtils {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 14 * 24 * 60 * 60; // 14일 (초 단위)

    @Value("${server.servlet.session.cookie.domain:}")
    private String cookieDomain;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * RefreshToken을 HttpOnly, Secure 쿠키로 설정
     *
     * @param response     HttpServletResponse
     * @param refreshToken RefreshToken 값
     * @param isSecure     Secure 플래그 (HTTPS 환경에서만 true)
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, boolean isSecure) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        if (cookieSameSite != null && !cookieSameSite.isBlank()) {
            builder.sameSite(cookieSameSite);
        }

        response.addHeader("Set-Cookie", builder.build().toString());

        log.debug("RefreshToken 쿠키 설정 완료 (Secure={})", isSecure);
    }

    /**
     * 쿠키에서 RefreshToken 추출
     *
     * @param request HttpServletRequest
     * @return RefreshToken (없으면 Optional.empty())
     */
    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * RefreshToken 쿠키 삭제 (로그아웃 시)
     *
     * @param response HttpServletResponse
     */
    public void clearRefreshTokenCookie(HttpServletResponse response, boolean isSecure) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(0);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        if (cookieSameSite != null && !cookieSameSite.isBlank()) {
            builder.sameSite(cookieSameSite);
        }

        response.addHeader("Set-Cookie", builder.build().toString());

        log.debug("RefreshToken 쿠키 삭제 완료");
    }

    /**
     * 범용 쿠키 생성
     *
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   유효 기간 (초)
     * @param isSecure Secure 플래그
     * @return Cookie 객체
     */
    public Cookie createCookie(String name, String value, int maxAge, boolean isSecure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
