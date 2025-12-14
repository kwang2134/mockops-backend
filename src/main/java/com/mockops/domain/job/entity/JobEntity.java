package com.mockops.domain.job.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * OpenAPI 파일 업로드 후 실행되는 비동기 벌크 작업의 상태와 최종 결과를 저장
 */
@Entity
@Table(name = "mock_api_bulk_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대상 Mock API 서버 ID
     */
    @Column(nullable = false, name = "server_id")
    private Long serverId;

    /**
     * 작업 상태 (PROCESSING, SUCCESS, FAILURE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    /**
     * 작업이 요청된 시각
     */
    @Column(nullable = false)
    private Instant submittedAt;

    /**
     * 작업이 완료된 시각
     */
    private Instant completedAt;

    /**
     * 파일에서 파싱된 총 API 개수
     */
    @Column(nullable = false)
    private Integer totalCount;

    /**
     * 성공적으로 DB에 삽입된 API 개수
     */
    @Column(nullable = false)
    private Integer insertedCount;

    /**
     * 실패 시 상세 오류 메시지
     */
    @Lob
    private String errorMessage;

    @Builder
    public JobEntity(Long serverId, JobStatus status, Instant submittedAt, Integer totalCount, Integer insertedCount) {
        this.serverId = serverId;
        this.status = status;
        this.submittedAt = submittedAt;
        this.totalCount = totalCount;
        this.insertedCount = insertedCount;
    }

    /**
     * 작업 완료 처리 (성공)
     */
    public void completeWithSuccess(Integer totalCount, Integer insertedCount) {
        this.status = JobStatus.SUCCESS;
        this.completedAt = Instant.now();
        this.totalCount = totalCount;
        this.insertedCount = insertedCount;
    }

    /**
     * 작업 실패 처리
     */
    public void completeWithFailure(String errorMessage) {
        this.status = JobStatus.FAILURE;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }
}