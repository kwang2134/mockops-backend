package com.mockops.presentation.api.user;

import com.mockops.domain.user.service.AuthService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.util.CookieUtils;
import com.mockops.presentation.api.user.docs.AuthDocs;
import com.mockops.presentation.api.user.dto.AccessTokenResponse;
import com.mockops.presentation.api.user.dto.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthDocs {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Value("${server.ssl.enabled:false}")
    private boolean isSecure;

    /**
     * Access Token 갱신
     * Refresh Token은 HttpOnly Cookie로 전달받고, 새로운 RefreshToken도 HttpOnly Cookie로만 응답
     * 응답 Body에는 Access Token만 포함됨
     */
    @Override
    @GetMapping("/token/refresh")
    public ResponseEntity<UnifiedResponse<AccessTokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = cookieUtils.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> ErrorCode.INVALID_TOKEN.serviceException("Refresh Token이 쿠키에 없습니다."));

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.info("쿠키 없음");
        }

        String refresh = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refresh = cookie.getValue();
                break;
            }
        }

        log.info("요청의 refresh={}", refresh);


        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);

        // 새로운 RefreshToken을 HttpOnly Cookie로 설정 (Token Rotation)
        cookieUtils.setRefreshTokenCookie(response, tokenResponse.refreshToken(), isSecure);

        log.info("새로 발급한 refresh={}", tokenResponse.refreshToken());

        // 응답 Body에는 Access Token만 포함
        return ResponseEntity.ok(UnifiedResponse.success(
                new AccessTokenResponse(tokenResponse.accessToken())
        ));
    }

    /**
     * 로그아웃
     * RefreshToken 쿠키를 삭제하고 DB의 RefreshToken도 무효화
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {
        authService.logout(userId);

        // RefreshToken 쿠키 삭제
        cookieUtils.clearRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }

    /**
     * OAuth 로그인 시작 (Google, GitHub)
     * TODO: Spring Security OAuth2 Client 사용하여 리다이렉트 처리
     */
    @Override
    @GetMapping("/{provider}/login")
    public ResponseEntity<Void> startOAuthLogin(@PathVariable String provider) {
        // Spring Security OAuth2가 자동으로 처리
        // 실제로는 302 Redirect가 발생
        log.info("OAuth login started for provider: {}", provider);
        return ResponseEntity.status(302).build();
    }
}
