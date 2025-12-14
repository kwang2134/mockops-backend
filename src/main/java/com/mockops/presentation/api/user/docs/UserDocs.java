package com.mockops.presentation.api.user.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.user.dto.UserAgreementRequest;
import com.mockops.presentation.api.user.dto.UserAgreementResponse;
import com.mockops.presentation.api.user.dto.UserDetailResponse;
import com.mockops.presentation.api.user.dto.UserUpdateNicknameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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

    @Operation(
            summary = "회원 탈퇴",
            description = "인증된 사용자의 회원 탈퇴를 처리합니다. " +
                    "사용자 정보는 익명화되며(deleted_[timestamp] 형태), 컬럼은 유지됩니다. " +
                    "프로젝트 멤버로 포함된 경우 프로젝트는 유지되며, 멤버 목록에서 '탈퇴한 사용자'로 표시됩니다. " +
                    "탈퇴 시 Refresh Token 쿠키가 만료되어 자동으로 로그아웃됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공",
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
    ResponseEntity<UnifiedResponse<Void>> withdrawUser(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "약관 동의 처리",
            description = "개인정보처리방침 및 이용약관에 대한 동의를 처리합니다. " +
                    "필수 동의 항목은 PRIVACY_POLICY(개인정보처리방침)과 TERMS_OF_SERVICE(이용약관)입니다. " +
                    "동의 이력은 버전별로 관리되며, 약관 변경 시 재동의가 필요할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 동의 처리 성공",
                    content = @Content(schema = @Schema(implementation = UserAgreementResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 데이터",
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
    ResponseEntity<UnifiedResponse<List<UserAgreementResponse>>> agreeToTerms(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserAgreementRequest request
    );

    @Operation(
            summary = "약관 동의 이력 조회",
            description = "인증된 사용자의 모든 약관 동의 이력을 조회합니다. " +
                    "동의한 약관 타입, 버전, 동의 시각 등을 최신순으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 동의 이력 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserAgreementResponse.class))
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
    ResponseEntity<UnifiedResponse<List<UserAgreementResponse>>> getUserAgreements(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
