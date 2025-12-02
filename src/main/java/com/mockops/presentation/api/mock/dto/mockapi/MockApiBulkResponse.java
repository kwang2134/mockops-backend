package com.mockops.presentation.api.mock.dto.mockapi;

/**
 * YAML/JSON 파일 업로드를 통한 Mock API 일괄 생성 응답
 */
public record MockApiBulkResponse(
        /**
         * 작업 ID (비동기 처리용)
         */
        String jobId,

        /**
         * 성공적으로 처리된 Mock API 건수
         */
        Integer processedCount,

        /**
         * 실패한 Mock API 건수
         */
        Integer failedCount,

        /**
         * 전체 파싱된 API 건수
         */
        Integer totalCount,

        /**
         * 처리 결과 메시지
         */
        String message
) {
    /**
     * 동기 처리 완료 응답 생성
     */
    public static MockApiBulkResponse of(int processedCount, int failedCount, int totalCount) {
        return new MockApiBulkResponse(
                null,
                processedCount,
                failedCount,
                totalCount,
                String.format("총 %d개 중 %d개 성공, %d개 실패", totalCount, processedCount, failedCount)
        );
    }

    /**
     * 비동기 처리 시작 응답 생성 (202 Accepted)
     */
    public static MockApiBulkResponse accepted(String jobId) {
        return new MockApiBulkResponse(
                jobId,
                null,
                null,
                null,
                "Mock API 대량 생성 작업이 시작되었습니다. jobId: " + jobId
        );
    }
}