package com.mockops.presentation.api.user.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.user.dto.UserDetailResponse;
import com.mockops.presentation.api.user.dto.UserUpdateNicknameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "사용자", description = "사용자 프로필 관리 API")
public interface UserDocs {

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "인증된 사용자의 상세 정보를 조회합니다. " +
                    "Access Token을 통해 사용자를 식별하며, 이메일, 닉네임, 프로필 정보 등을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<UserDetailResponse>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "사용자 닉네임 수정",
            description = "인증된 사용자의 닉네임을 수정합니다. " +
                    "닉네임은 1자 이상 20자 이하여야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 닉네임 형식",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<UserDetailResponse>> updateNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserUpdateNicknameRequest request
    );
}
