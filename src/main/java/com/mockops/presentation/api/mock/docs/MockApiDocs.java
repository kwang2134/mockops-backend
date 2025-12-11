package com.mockops.presentation.api.mock.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.job.dto.JobStatusResponse;
import com.mockops.presentation.api.mock.dto.mockapi.*;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Mock API", description = "Mock API 관리 API - 가상의 HTTP 엔드포인트를 생성하고 관리합니다")
public interface MockApiDocs {

    @Operation(
            summary = "서버의 Mock API 목록 조회",
            description = "특정 도메인 서버에 속한 모든 Mock API 목록을 복합 커서 기반 페이지네이션으로 조회합니다. " +
                    "name(그룹 이름) 오름차순 → ID 오름차순으로 정렬되어 같은 그룹의 API가 함께 표시됩니다. " +
                    "각 Mock API의 HTTP 메서드, 엔드포인트 경로, 상태 코드, 활성화 여부 등의 정보를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = MockApiListResponse.class))
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
    ResponseEntity<UnifiedResponse<MockApiListResponse>> getMockApisByServer(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(description = "마지막 name 커서 (다음 페이지 조회 시)", example = "users")
            @RequestParam(required = false) String lastNameCursor,
            @Parameter(description = "마지막 ID 커서 (다음 페이지 조회 시)", example = "123")
            @RequestParam(required = false) Long lastIdCursor,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Mock API 상세 조회",
            description = "특정 Mock API의 상세 정보를 조회합니다. " +
                    "HTTP 메서드, 엔드포인트 경로, 응답 본문(responseBody), 상태 코드 등 모든 설정 정보를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = MockApiDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<MockApiDetailResponse>> getMockApi(
            @Parameter(description = "Mock API ID", example = "1")
            @PathVariable Long mockApiId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Mock API 생성",
            description = "서버에 새로운 Mock API를 생성합니다. " +
                    "HTTP 메서드, 엔드포인트 경로, 응답 본문, 상태 코드를 설정할 수 있습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 생성할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Mock API 생성 성공",
                    content = @Content(schema = @Schema(implementation = MockApiCreateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 데이터 또는 중복된 엔드포인트",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 생성 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<MockApiCreateResponse>> createMockApi(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Valid @RequestBody MockApiCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "OpenAPI 스펙 파일 업로드",
            description = "OpenAPI(Swagger) 스펙 파일을 업로드하여 여러 Mock API를 일괄 생성합니다. " +
                    "JSON 또는 YAML 형식의 OpenAPI 3.0 스펙 파일을 지원합니다. " +
                    "파일에 정의된 각 경로(path)와 메서드에 대해 Mock API가 생성됩니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 업로드할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Mock API 일괄 생성 성공",
                    content = @Content(schema = @Schema(implementation = MockApiBulkResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 파일 형식 또는 파싱 오류",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 생성 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<MockApiBulkResponse>> uploadMockApis(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(description = "OpenAPI 스펙 파일 (JSON 또는 YAML)")
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Mock API 수정",
            description = "Mock API의 정보를 수정합니다. " +
                    "HTTP 메서드, 엔드포인트 경로, 응답 본문, 상태 코드, 활성화 여부를 변경할 수 있습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 수정 성공",
                    content = @Content(schema = @Schema(implementation = MockApiUpdateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 데이터 또는 중복된 엔드포인트",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 수정 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<MockApiUpdateResponse>> updateMockApi(
            @Parameter(description = "Mock API ID", example = "1")
            @PathVariable Long mockApiId,
            @Valid @RequestBody MockApiUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Mock API 삭제",
            description = "Mock API를 삭제합니다. " +
                    "삭제된 Mock API는 더 이상 호출할 수 없습니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Mock API 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 삭제 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<Void> deleteMockApi(
            @Parameter(description = "Mock API ID", example = "1")
            @PathVariable Long mockApiId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "Mock API 활성화 상태 토글",
            description = "Mock API의 활성화 상태를 토글합니다. " +
                    "비활성화된 Mock API는 호출 시 404 Not Found를 반환합니다. " +
                    "활성화/비활성화를 빠르게 전환할 수 있어 테스트 시나리오 관리에 유용합니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 변경할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 상태 토글 성공",
                    content = @Content(schema = @Schema(implementation = MockApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mock API 수정 권한 없음 (OWNER 또는 EDITOR만 가능)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<MockApiResponse>> toggleMockApiStatus(
            @Parameter(description = "Mock API ID", example = "1")
            @PathVariable Long mockApiId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "서버의 진행 중인 Job 조회",
            description = "특정 서버에 현재 진행 중인(PROCESSING 상태) Mock API 일괄 생성 Job을 조회합니다. " +
                    "파일 업로드를 통한 Mock API 생성은 서버당 한 번에 하나의 Job만 처리되므로, " +
                    "프론트엔드에서 이 API를 통해 진행 중인 Job이 있는지 확인하여 " +
                    "중복 업로드를 방지할 수 있습니다. " +
                    "진행 중인 Job이 없으면 404 Not Found를 반환합니다. " +
                    "DEVELOPER 이상의 권한을 가진 멤버만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "진행 중인 Job 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "서버 접근 권한 없음 (DEVELOPER 이상 필요)",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "서버를 찾을 수 없거나 진행 중인 Job이 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<JobStatusResponse>> getActiveJob(
            @Parameter(description = "서버 ID", example = "1")
            @PathVariable Long serverId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
