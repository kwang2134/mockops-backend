package com.mockops.presentation.api.mock.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Public Mock API", description = "Mock API 요청 수신 엔드포인트 (인증 불필요) - 실제 처리는 MockOpsHandlerMapping에서 수행됩니다")
public interface PublicMockDocs {

    @Operation(
            summary = "Mock API 요청 (GET)",
            description = "등록된 Mock API로 GET 요청을 보냅니다. " +
                    "프로젝트 ID와 서버 이름으로 Mock API를 식별하며, 나머지 경로는 엔드포인트 경로로 사용됩니다. " +
                    "예: /mock/1/my-server/api/users → projectId=1, serverName=my-server, path=/api/users\n\n" +
                    "**주의**: 이 컨트롤러는 Swagger 문서 생성용이며, 실제 요청은 MockOpsHandlerMapping에서 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Mock response\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없거나 비활성화 상태"
            )
    })
    ResponseEntity<String> handleGetRequest(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "서버 이름", example = "my-server")
            @PathVariable String serverName
    );

    @Operation(
            summary = "Mock API 요청 (POST)",
            description = "등록된 Mock API로 POST 요청을 보냅니다. " +
                    "요청 본문은 Mock API 설정에 관계없이 무시되며, 설정된 응답 본문이 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"created\": true}")
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Mock API 생성 응답 (설정에 따라)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없거나 비활성화 상태"
            )
    })
    ResponseEntity<String> handlePostRequest(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "서버 이름", example = "my-server")
            @PathVariable String serverName
    );

    @Operation(
            summary = "Mock API 요청 (PUT)",
            description = "등록된 Mock API로 PUT 요청을 보냅니다. " +
                    "요청 본문은 Mock API 설정에 관계없이 무시되며, 설정된 응답 본문이 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"updated\": true}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없거나 비활성화 상태"
            )
    })
    ResponseEntity<String> handlePutRequest(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "서버 이름", example = "my-server")
            @PathVariable String serverName
    );

    @Operation(
            summary = "Mock API 요청 (DELETE)",
            description = "등록된 Mock API로 DELETE 요청을 보냅니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 응답 성공"
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Mock API 삭제 응답 (설정에 따라)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없거나 비활성화 상태"
            )
    })
    ResponseEntity<String> handleDeleteRequest(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "서버 이름", example = "my-server")
            @PathVariable String serverName
    );

    @Operation(
            summary = "Mock API 요청 (PATCH)",
            description = "등록된 Mock API로 PATCH 요청을 보냅니다. " +
                    "요청 본문은 Mock API 설정에 관계없이 무시되며, 설정된 응답 본문이 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock API 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"patched\": true}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mock API를 찾을 수 없거나 비활성화 상태"
            )
    })
    ResponseEntity<String> handlePatchRequest(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "서버 이름", example = "my-server")
            @PathVariable String serverName
    );
}
