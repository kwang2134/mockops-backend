package com.mockops.presentation.api.webhook.docs;

import com.mockops.presentation.api.webhook.dto.DeploymentEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Webhook", description = "CI/CD 배포 이벤트 Webhook 수신 API")
public interface WebhookDocs {

    @Operation(
            summary = "CI/CD 배포 완료 Webhook 수신",
            description = "CI/CD 파이프라인에서 배포 완료 시 호출되는 Webhook 엔드포인트입니다. " +
                    "배포 상태(SUCCESS, FAILURE)에 따라 헬스체크를 수행하거나 알림을 전송합니다. " +
                    "요청은 비동기로 처리되며, 202 Accepted를 즉시 반환합니다.\n\n" +
                    "**필수 정보**:\n" +
                    "- **프로젝트 이름**: 배포 대상 프로젝트의 이름\n" +
                    "- **도메인 서버 이름**: 배포 상태를 변경할 특정 도메인 서버의 이름 (도메인 서버 이름은 프로젝트 간 겹칠 수 있으므로 프로젝트 이름과 함께 사용)\n\n" +
                    "**인증**: Authorization 헤더에 Bearer 토큰(Webhook JWT)이 필요합니다. " +
                    "토큰은 프로젝트별 Webhook Secret을 통해 생성됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Webhook 수신 성공 (비동기 처리)",
                    headers = @Header(
                            name = "Content-Type",
                            description = "응답 콘텐츠 타입",
                            schema = @Schema(type = "string", defaultValue = "application/json")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 데이터",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Webhook 토큰 또는 프로젝트 ID 불일치",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    ResponseEntity<Void> receiveDeploymentEvent(
            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable Long projectId,
            @Parameter(description = "Webhook 인증 토큰 (Bearer {JWT})", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeploymentEventRequest request
    );
}
