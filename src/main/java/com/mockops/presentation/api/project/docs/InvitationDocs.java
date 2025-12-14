package com.mockops.presentation.api.project.docs;

import com.mockops.domain.project.entity.InvitationStatus;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.invitation.InvitationCreateRequest;
import com.mockops.presentation.api.project.dto.invitation.InvitationCreateResponse;
import com.mockops.presentation.api.project.dto.invitation.PagedInvitationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "프로젝트 초대", description = "프로젝트 초대 관리 API")
public interface InvitationDocs {

    @Operation(
            summary = "팀원 초대 생성",
            description = "이메일을 통해 프로젝트에 팀원을 초대합니다. " +
                    "초대 링크가 포함된 이메일이 발송되며, 초대받은 사용자는 링크를 통해 프로젝트에 참여할 수 있습니다. " +
                    "OWNER 또는 MANAGER 권한을 가진 멤버만 초대할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "초대 생성 및 발송 성공",
                    content = @Content(schema = @Schema(implementation = InvitationCreateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 이메일 또는 이미 멤버인 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "초대 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<InvitationCreateResponse>> createInvitation(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Valid @RequestBody InvitationCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "초대 목록 조회",
            description = "프로젝트의 초대 목록을 조회합니다. " +
                    "상태(PENDING, ACCEPTED, REJECTED, EXPIRED)로 필터링할 수 있으며, 페이지네이션을 지원합니다. " +
                    "OWNER 또는 MANAGER 권한을 가진 멤버만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "초대 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PagedInvitationListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "프로젝트 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<PagedInvitationListResponse>> getInvitations(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "초대 상태 필터 (PENDING, ACCEPTED, REJECTED, EXPIRED)", example = "PENDING")
            @RequestParam(required = false) InvitationStatus status,
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=10&sort=createdAt,desc")
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "초대 취소",
            description = "발송된 초대를 취소합니다. " +
                    "PENDING 상태의 초대만 취소할 수 있으며, OWNER 또는 MANAGER 권한을 가진 멤버만 취소할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "초대 취소 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 처리된 초대이거나 취소할 수 없는 상태",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "초대 취소 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 초대를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> cancelInvitation(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "초대 ID", example = "10")
            @PathVariable Long invitationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "초대 수락",
            description = "이메일로 받은 초대 토큰을 사용하여 프로젝트 초대를 수락합니다. " +
                    "로그인된 사용자가 유효한 토큰과 함께 요청하면 프로젝트 멤버로 등록됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "초대 수락 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않거나 만료된 토큰",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "초대를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<String>> acceptInvitation(
            @Parameter(description = "초대 토큰", example = "abc123xyz789")
            @RequestParam String token,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
