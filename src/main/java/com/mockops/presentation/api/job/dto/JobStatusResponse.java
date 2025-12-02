package com.mockops.presentation.api.job.dto;

import com.mockops.domain.job.entity.JobEntity;

import java.time.Instant;

/**
 * 비동기 작업 상태 조회 응답 DTO
 *
 * @param jobId Job의 고유 ID
 * @param status 작업 상태 (PROCESSING, SUCCESS, FAILURE)
 * @param submittedAt 작업 요청 시간
 * @param completedAt 작업 완료 시간
 * @param totalParsed 파일에서 파싱된 총 API 개수
 * @param successCount 성공적으로 삽입된 API 개수
 * @param duplicateCount 중복으로 인해 스킵된 API 개수
 * @param message 사용자에게 보여줄 간단한 성공/실패 메시지
 * @param detailedError 실패 시 상세 에러 로그 (개발자/운영자용)
 */
public record JobStatusResponse(
        Long jobId,
        String status,
        Instant submittedAt,
        Instant completedAt,
        int totalParsed,
        int successCount,
        int duplicateCount,
        String message,
        String detailedError
) {
    /**
     * JobEntity로부터 JobStatusResponse 생성
     */
    public static JobStatusResponse from(JobEntity job) {
        int duplicateCount = job.getTotalCount() - job.getInsertedCount();
        String message = generateMessage(job);

        return new JobStatusResponse(
                job.getId(),
                job.getStatus().name(),
                job.getSubmittedAt(),
                job.getCompletedAt(),
                job.getTotalCount(),
                job.getInsertedCount(),
                duplicateCount,
                message,
                job.getErrorMessage()
        );
    }

    /**
     * 작업 상태에 따른 메시지 생성
     */
    private static String generateMessage(JobEntity job) {
        return switch (job.getStatus()) {
            case PROCESSING -> "Mock API 대량 생성 작업이 진행 중입니다.";
            case SUCCESS -> String.format(
                    "총 %d개 중 %d개 성공, %d개 중복으로 스킵되었습니다.",
                    job.getTotalCount(),
                    job.getInsertedCount(),
                    job.getTotalCount() - job.getInsertedCount()
            );
            case FAILURE -> "Mock API 대량 생성 작업이 실패했습니다.";
        };
    }
}
