package com.mockops.domain.job.repository;

import com.mockops.domain.job.entity.JobEntity;
import com.mockops.domain.job.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long> {

    /**
     * 특정 서버의 특정 상태인 Job 목록 조회 (최신순)
     * 동시성 제어가 프론트엔드에서만 이루어지므로 여러 개의 PROCESSING 상태 Job이 존재할 수 있음
     * @param serverId 서버 ID
     * @param status Job 상태
     * @return 해당하는 Job 목록 (최신순)
     */
    List<JobEntity> findByServerIdAndStatusOrderBySubmittedAtDesc(Long serverId, JobStatus status);
}
