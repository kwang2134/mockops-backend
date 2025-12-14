package com.mockops.presentation.api.webhook.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.webhook.dto.WebhookSecretResponse;
import com.mockops.presentation.api.webhook.dto.WebhookTokenIssueRequest;
import com.mockops.presentation.api.webhook.dto.WebhookTokenResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Webhook Secret", description = "Webhook Secret Key 및 인증 토큰 관리 API (OWNER 권한 필요)")
public interface WebhookSecretDocs {

    @Operation(
            summary = "Secret Key 정보 조회",
            description = "프로젝트의 Webhook Secret Key 정보를 조회합니다. " +
                    "보안을 위해 실제 Secret Key 값은 마스킹되어 반환되며, 생성일, 활성화 상태 등의 메타데이터만 확인할 수 있습니다. " +
                    "OWNER 권한을 가진 멤버만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Secret Key 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = WebhookSecretResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Secret Key 조회 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 Secret Key를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<WebhookSecretResponse>> getSecretInfo(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Secret Key 재발급",
            description = "Webhook Secret Key를 재발급합니다. " +
                    "기존 Secret Key는 즉시 폐기되며, 이전 키로 생성된 JWT 토큰은 더 이상 유효하지 않습니다. " +
                    "재발급된 키는 응답에 포함되므로 안전하게 보관해야 합니다. " +
                    "OWNER 권한을 가진 멤버만 재발급할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Secret Key 재발급 성공",
                    content = @Content(schema = @Schema(implementation = WebhookSecretResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Secret Key 재발급 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<WebhookSecretResponse>> reissueSecretKey(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Secret Key 비활성화",
            description = "Webhook Secret Key를 비활성화합니다. " +
                    "비활성화된 키로는 더 이상 JWT 토큰을 발급할 수 없으며, 기존에 발급된 토큰도 유효하지 않게 됩니다. " +
                    "Webhook 기능을 일시적으로 중단하고자 할 때 사용합니다. " +
                    "OWNER 권한을 가진 멤버만 비활성화할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Secret Key 비활성화 성공",
                    content = @Content(schema = @Schema(implementation = WebhookSecretResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Secret Key 비활성화 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 Secret Key를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<WebhookSecretResponse>> deactivateSecretKey(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Webhook JWT 토큰 발급",
            description = "CI/CD 파이프라인에서 사용할 Webhook JWT 토큰을 발급합니다. " +
                    "발급된 토큰은 30일간 유효하며, Webhook 요청 시 Authorization 헤더에 포함하여 사용합니다. " +
                    "Secret Key를 요청 본문에 포함하여 검증 후 토큰을 발급합니다. " +
                    "OWNER 권한을 가진 멤버만 발급할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Webhook JWT 토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = WebhookTokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 Secret Key",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "토큰 발급 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 Secret Key를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<WebhookTokenResponse>> issueWebhookToken(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Valid @RequestBody WebhookTokenIssueRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
