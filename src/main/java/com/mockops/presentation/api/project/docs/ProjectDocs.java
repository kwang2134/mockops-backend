package com.mockops.presentation.api.project.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.project.dto.project.*;
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

@Tag(name = "프로젝트", description = "프로젝트 관리 API")
public interface ProjectDocs {

    @Operation(
            summary = "프로젝트 생성",
            description = "새로운 프로젝트를 생성합니다. " +
                    "프로젝트 생성 시 요청한 사용자가 자동으로 OWNER 권한의 멤버로 등록됩니다. " +
                    "Webhook Secret도 자동으로 생성되며, 평문으로 응답에 포함됩니다. " +
                    "이 Secret은 생성 시에만 노출되므로 반드시 안전한 곳에 저장해야 합니다. " +
                    "Slack 웹훅 URL을 설정하면 프로젝트 관련 알림을 Slack으로 받을 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "프로젝트 생성 성공 (Webhook Secret 평문 포함)",
                    content = @Content(schema = @Schema(implementation = ProjectCreateResponse.class))
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
            )
    })
    ResponseEntity<UnifiedResponse<ProjectCreateResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "내 프로젝트 목록 조회",
            description = "현재 사용자가 속한 모든 프로젝트 목록을 페이지 단위로 조회합니다. " +
                    "페이지네이션을 지원하며, 각 프로젝트의 기본 정보와 멤버 수, 서버 수 등의 통계를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<ProjectPageResponse>> getMyProjects(
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=10&sort=createdAt,desc")
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "특정 프로젝트의 상세 정보를 조회합니다. " +
                    "프로젝트 멤버만 조회할 수 있으며, 프로젝트의 기본 정보와 설정, 통계 등을 포함합니다. " +
                    "응답에는 현재 로그인된 사용자의 프로젝트 멤버 권한(currentUserMemberRole)이 포함되어, " +
                    "프론트엔드에서 권한별 UI 조건부 렌더링(수정/삭제 버튼 등)에 활용할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))
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
    ResponseEntity<UnifiedResponse<ProjectDetailResponse>> getProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "프로젝트 정보 수정",
            description = "프로젝트의 설명과 Slack 웹훅 URL을 수정합니다. " +
                    "OWNER 권한을 가진 멤버만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 수정 성공",
                    content = @Content(schema = @Schema(implementation = ProjectUpdateResponse.class))
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
                    responseCode = "403",
                    description = "프로젝트 수정 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<ProjectUpdateResponse>> updateProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "프로젝트 삭제",
            description = "프로젝트를 삭제합니다. " +
                    "OWNER 권한을 가진 멤버만 삭제할 수 있으며, 프로젝트에 속한 모든 데이터(서버, Mock API, 멤버 등)가 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "프로젝트 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "프로젝트 삭제 권한 없음 (OWNER만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> deleteProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "프로젝트 검색",
            description = "프로젝트 제목과 오너 ID로 프로젝트를 검색합니다. " +
                    "검색 조건은 선택사항이며, 모든 조건을 생략하면 전체 프로젝트 목록이 반환됩니다. " +
                    "인증된 사용자만 검색할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 검색 성공",
                    content = @Content(schema = @Schema(implementation = ProjectPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<ProjectPageResponse>> searchProjects(
            @Parameter(description = "프로젝트 제목 (부분 일치)", example = "MockOps")
            String name,
            @Parameter(description = "프로젝트 오너 Nickname", example = "kim")
            String ownerNickname,
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=10&sort=createdAt,desc")
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
