package com.mockops.presentation.api.job.docs;

import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.job.dto.JobStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Job 추적", description = "비동기 작업 상태 조회 API")
public interface JobDocs {

    @Operation(
            summary = "비동기 작업 상태 조회",
            description = "비동기로 처리되는 벌크 작업(OpenAPI 파일 업로드 등)의 상태를 조회합니다. " +
                    "작업 상태는 PROCESSING(진행 중), COMPLETED(완료), FAILED(실패) 중 하나입니다. " +
                    "폴링(Polling) 방식으로 주기적으로 호출하여 작업 완료 여부를 확인할 수 있습니다.\n\n" +
                    "**작업 흐름**:\n" +
                    "1. 벌크 작업 API 호출 → Job ID 반환\n" +
                    "2. 이 엔드포인트로 Job ID 조회 (폴링)\n" +
                    "3. 상태가 COMPLETED 또는 FAILED가 될 때까지 반복"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "작업 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "작업 조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "작업을 찾을 수 없음 또는 만료됨",
                    content = @Content(schema = @Schema(implementation = UnifiedResponse.class))
            )
    })
    ResponseEntity<UnifiedResponse<JobStatusResponse>> getJobStatus(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long jobId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}
