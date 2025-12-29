package com.mockops.presentation.api.mock.docs;

import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.mock.dto.domainserver.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "도메인 서버", description = "도메인 서버 관리 API - Mock API를 제공하는 서버를 관리합니다")
public interface DomainServerDocs {

    @Operation(
            summary = "프로젝트의 서버 목록 조회",
            description = "특정 프로젝트에 속한 모든 도메인 서버 목록을 페이지 단위로 조회합니다. " +
                    "각 서버의 기본 정보(이름, slug, 상태)와 미확인 알림 개수를 포함합니다. " +
                    "프로젝트 멤버만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "서버 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerSimpleResponse.class))
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
    ResponseEntity<UnifiedResponse<Page<DomainServerSimpleResponse>>> getServersByProject(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=20&sort=id,desc")
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "서버 상세 조회",
            description = "특정 도메인 서버의 상세 정보를 조회합니다. " +
                    "서버의 상태, 헬스체크 설정, 마지막 헬스체크 시각 등의 정보를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "서버 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "서버 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<DomainServerResponse>> getServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "서버 생성",
            description = "프로젝트에 새로운 도메인 서버를 생성합니다. " +
                    "서버 이름은 필수이며, 헬스체크 URL과 주기를 설정할 수 있습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 생성할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "서버 생성 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerCreateResponse.class))
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
                    description = "서버 생성 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<DomainServerCreateResponse>> createServer(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Valid @RequestBody DomainServerCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "서버 정보 수정",
            description = "도메인 서버의 정보를 수정합니다. " +
                    "서버 이름, 상태, 헬스체크 설정 등을 변경할 수 있습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "서버 수정 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerUpdateResponse.class))
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
                    description = "서버 수정 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<DomainServerUpdateResponse>> updateServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Valid @RequestBody DomainServerUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "서버 삭제",
            description = "도메인 서버를 삭제합니다. " +
                    "서버에 속한 모든 Mock API도 함께 삭제됩니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "서버 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "서버 삭제 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> deleteServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "도메인 서버 검색",
            description = "프로젝트 내 도메인 서버를 이름과 상태로 검색합니다. " +
                    "검색 조건은 선택사항이며, 모든 조건을 생략하면 전체 서버 목록이 반환됩니다. " +
                    "프로젝트 멤버(VIEWER) 이상만 검색할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "도메인 서버 검색 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerSimpleResponse.class))
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
    ResponseEntity<UnifiedResponse<Page<DomainServerSimpleResponse>>> searchDomainServers(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "도메인 서버 이름 (부분 일치)", example = "API Server")
            String name,
            @Parameter(description = "서버 상태 (MOCKING, PROXYING, DOWN)", example = "MOCKING")
            ServerStatus status,
            @Parameter(description = "페이지 정보 (page, size, sort)", example = "page=0&size=20&sort=createdAt,desc")
            Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "도메인 서버 담당 멤버 목록 조회",
            description = "특정 도메인 서버를 담당하는 멤버 목록을 조회합니다. " +
                    "Offset 기반 페이지네이션을 사용하며, 권한 순서(OWNER → MANAGER → DEVELOPER → VIEWER)로 정렬됩니다. " +
                    "응답에는 로그인한 사용자의 도메인 서버 참여 정보도 포함됩니다. " +
                    "프로젝트 멤버(VIEWER) 이상만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "담당 멤버 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = DomainServerMemberListResponse.class))
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
                    description = "도메인 서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<DomainServerMemberListResponse>> getDomainServerMembers(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "오프셋 (건너뛸 항목 수)", example = "0")
            @RequestParam(required = false) Integer offset,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "도메인 서버 참여",
            description = "로그인한 사용자가 도메인 서버의 담당 멤버로 참여합니다. " +
                    "한 명의 멤버는 하나의 도메인 서버만 담당할 수 있습니다. " +
                    "이미 다른 도메인 서버를 담당 중인 경우 참여할 수 없습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 참여할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "도메인 서버 참여 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 다른 도메인 서버에 참여 중",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "도메인 서버 참여 권한 없음 (DEVELOPER 이상만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "도메인 서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> joinDomainServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "도메인 서버 나가기",
            description = "로그인한 사용자가 담당 중인 도메인 서버에서 나갑니다. " +
                    "담당하지 않는 도메인 서버에서는 나갈 수 없습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 나갈 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "도메인 서버 나가기 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 도메인 서버를 담당하고 있지 않음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (DEVELOPER 이상만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "도메인 서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> leaveDomainServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
