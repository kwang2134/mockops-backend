package com.mockops.presentation.api.job;

import com.mockops.domain.job.service.JobTrackingService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.job.dto.JobStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Job Tracking 컨트롤러
 * 비동기 벌크 작업의 상태를 조회
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobTrackingService jobTrackingService;

    /**
     * 비동기 벌크 작업 상태 조회 (Polling)
     * GET /api/v1/jobs/{jobId}
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<UnifiedResponse<JobStatusResponse>> getJobStatus(
            @PathVariable Long jobId,
            @AuthenticationPrincipal Long userId
    ) {
        log.info("Job 상태 조회 요청: jobId={}, userId={}", jobId, userId);

        JobStatusResponse response = jobTrackingService.getJobStatusWithPermission(jobId, userId);

        return ResponseEntity.ok(UnifiedResponse.success(response));
    }
}
