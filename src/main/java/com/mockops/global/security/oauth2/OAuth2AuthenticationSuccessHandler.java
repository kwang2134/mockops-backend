package com.mockops.global.security.oauth2;

import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.repository.AuthProviderRepository;
import com.mockops.global.security.JwtProvider;
import com.mockops.global.util.CookieUtils;
import com.mockops.global.util.CryptUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * OAuth2 인증 성공 시 처리 핸들러
 * 업계 표준 베스트 프랙티스:
 * 1. RefreshToken만 HttpOnly 쿠키로 설정
 * 2. AccessToken은 전달하지 않음
 * 3. 프론트엔드가 리다이렉트 후 /api/v1/auth/token/refresh 호출하여 AccessToken 발급
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final CryptUtils cryptUtils;
    private final CookieUtils cookieUtils;
    private final AuthProviderRepository authProviderRepository;

    @Value("${server.ssl.enabled:false}")
    private boolean isSecure;

    // TODO: 프론트엔드 리다이렉트 URL을 application.yml에 설정
    @Value("${oauth2.redirect-url:http://localhost:3000/oauth2/callback}")
    private String redirectUrl;

    /**
     * OAuth2 인증 성공 시 호출됨
     * 1. RefreshToken 생성 및 DB 저장
     * 2. RefreshToken을 HttpOnly 쿠키로 설정
     * 3. 쿼리 파라미터 없이 프론트엔드로 리다이렉트
     * 4. 프론트엔드가 /api/v1/auth/token/refresh 호출하여 AccessToken 발급
     *
     * @param request        HttpServletRequest
     * @param response       HttpServletResponse
     * @param authentication OAuth2 인증 정보 (CustomOAuth2User 포함)
     * @throws IOException      I/O 에러
     * @throws ServletException Servlet 에러
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();

        log.info("OAuth2 인증 성공: userId={}", userId);

        // RefreshToken 생성
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        // RefreshToken을 암호화하여 DB에 저장 (모든 AuthProvider 업데이트)
        String encryptedRefreshToken = cryptUtils.encrypt(refreshToken);
        List<AuthProvider> authProviders = authProviderRepository.findByUserId(userId);
        authProviders.forEach(provider -> provider.updateRefreshToken(encryptedRefreshToken));

        log.info("RefreshToken 저장 완료: userId={}, providerCount={}", userId, authProviders.size());

        // RefreshToken을 HttpOnly 쿠키로 설정
        cookieUtils.setRefreshTokenCookie(response, refreshToken, isSecure);

        // 프론트엔드로 리다이렉트 (쿼리 파라미터 없음)
        log.info("OAuth2 인증 완료, 리다이렉트: userId={}, targetUrl={}", userId, redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
