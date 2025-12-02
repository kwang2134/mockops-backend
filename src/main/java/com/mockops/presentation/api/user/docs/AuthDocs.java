package com.mockops.presentation.api.user.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.user.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "인증", description = "사용자 인증 및 토큰 관리 API")
public interface AuthDocs {

    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다. " +
                    "Refresh Token은 HttpOnly Cookie로 전달받으며, 새로운 Refresh Token도 Cookie로 응답됩니다. " +
                    "Token Rotation 방식을 사용하여 보안을 강화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh Token 또는 만료된 토큰",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<TokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = "사용자를 로그아웃 처리합니다. Refresh Token 쿠키를 삭제하고 DB에 저장된 Refresh Token을 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    );

    @Operation(
            summary = "OAuth 로그인 시작",
            description = "지정된 OAuth 제공자(Google, GitHub)를 통한 로그인을 시작합니다. " +
                    "Spring Security OAuth2 Client가 자동으로 OAuth 제공자의 로그인 페이지로 리다이렉트합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "OAuth 제공자 로그인 페이지로 리다이렉트"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "지원하지 않는 OAuth 제공자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> startOAuthLogin(
            @Parameter(description = "OAuth 제공자 (google, github)", example = "google")
            @PathVariable String provider
    );
}
