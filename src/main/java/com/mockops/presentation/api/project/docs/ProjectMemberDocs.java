package com.mockops.presentation.api.project.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.MemberInviteAcceptRequest;
import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberListResponse;
import com.mockops.presentation.api.project.dto.projectmember.MemberRoleUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "프로젝트 멤버", description = "프로젝트 멤버 관리 API")
public interface ProjectMemberDocs {

    @Operation(
            summary = "알림을 통한 초대 수락",
            description = "웹 서비스를 통해 알림으로 받은 초대를 수락합니다. " +
                    "로그인된 사용자가 알림의 초대를 수락하여 프로젝트 멤버로 참여합니다. " +
                    "초대장에 명시된 역할로 프로젝트에 합류하게 됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "초대 수락 성공",
                    content = @Content(schema = @Schema(implementation = ProjectMemberResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 또는 만료된 초대장",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인의 초대만 수락 가능",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 초대장을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<ProjectMemberResponse>> acceptMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Valid @RequestBody MemberInviteAcceptRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "프로젝트 팀원 목록 조회",
            description = "프로젝트에 속한 모든 팀원 목록을 조회합니다. " +
                    "커서 기반 페이지네이션을 사용하며, 각 멤버의 정보와 권한을 포함합니다. " +
                    "프로젝트 멤버만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "팀원 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberListResponse.class))
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
    ResponseEntity<UnifiedResponse<MemberListResponse>> getMembers(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "커서 ID (다음 페이지 조회 시 마지막 멤버 ID)", example = "123")
            @RequestParam(required = false) Long cursorId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "팀원 역할 변경",
            description = "프로젝트 팀원의 역할을 변경합니다. " +
                    "MANAGER 이상의 권한을 가진 멤버만 변경할 수 있으며, OWNER 역할은 변경할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "팀원 역할 변경 성공",
                    content = @Content(schema = @Schema(implementation = ProjectMemberResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 역할 또는 OWNER 역할 변경 시도",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "팀원 역할 변경 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 멤버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<ProjectMemberResponse>> updateMemberRole(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "멤버 ID", example = "10")
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "팀원 제외",
            description = "프로젝트에서 팀원을 제외합니다. " +
                    "MANAGER 이상의 권한을 가진 멤버만 제외할 수 있으며, OWNER 자신은 제외할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "팀원 제외 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "OWNER 자신을 제외하려는 시도",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "팀원 제외 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 멤버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> removeMember(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "멤버 ID", example = "10")
            @PathVariable Long memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
