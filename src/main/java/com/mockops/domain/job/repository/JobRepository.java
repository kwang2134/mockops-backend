package com.mockops.domain.job.repository;

import com.mockops.domain.job.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
}
