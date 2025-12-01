package com.mockops.domain.job.entity;

/**
 * 비동기 작업 상태를 나타내는 Enum
 */
public enum JobStatus {
    /**
     * 작업이 백그라운드에서 실행 중
     */
    PROCESSING,

    /**
     * 작업이 성공적으로 완료됨
     */
    SUCCESS,

    /**
     * 작업 실행 중 오류가 발생하여 실패함
     */
    FAILURE
}