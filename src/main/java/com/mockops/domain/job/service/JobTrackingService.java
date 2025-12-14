package com.mockops.domain.job.service;

import com.mockops.domain.job.entity.JobEntity;
import com.mockops.domain.job.entity.JobStatus;
import com.mockops.domain.job.repository.JobRepository;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.service.DomainServerService;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.job.dto.JobStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Job Tracking 서비스
 * 비동기 벌크 작업의 생성, 조회, 상태 업데이트를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobTrackingService {

    private final JobRepository jobRepository;
    private final DomainServerService domainServerService;
    private final ProjectMemberService projectMemberService;

    /**
     * 새로운 Job 생성 (PROCESSING 상태로 시작)
     *
     * @param serverId 서버 ID
     * @return 생성된 JobEntity
     */
    @Transactional
    public JobEntity createJob(Long serverId) {
        JobEntity job = JobEntity.builder()
                .serverId(serverId)
                .status(JobStatus.PROCESSING)
                .submittedAt(Instant.now())
                .totalCount(0)
                .insertedCount(0)
                .build();

        JobEntity savedJob = jobRepository.save(job);

        log.info("Job 생성 완료: jobId={}, serverId={}, status={}",
                savedJob.getId(), serverId, JobStatus.PROCESSING);

        return savedJob;
    }

    /**
     * Job ID로 조회 - DTO 반환 (권한 검증 포함)
     *
     * @param jobId Job ID
     * @param currentUserId 현재 사용자 ID
     * @return JobStatusResponse
     */
    public JobStatusResponse getJobStatusWithPermission(Long jobId, Long currentUserId) {
        JobEntity job = getJobById(jobId);

        // 권한 검증: 해당 Job의 서버가 속한 프로젝트의 DEVELOPER 이상 멤버만 조회 가능
        DomainServer server = domainServerService.getDomainServerById(job.getServerId());
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        return JobStatusResponse.from(job);
    }

    /**
     * Job ID로 조회 - DTO 반환 (내부용, 권한 검증 없음)
     *
     * @param jobId Job ID
     * @return JobStatusResponse
     */
    public JobStatusResponse getJobStatusById(Long jobId) {
        JobEntity job = getJobById(jobId);
        return JobStatusResponse.from(job);
    }

    /**
     * Job ID로 조회 - 내부용
     *
     * @param jobId Job ID
     * @return JobEntity
     */
    public JobEntity getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> ErrorCode.JOB_NOT_FOUND.domainException(
                        "존재하지 않는 Job입니다. jobId=" + jobId
                ));
    }

    /**
     * Job 성공 처리
     *
     * @param jobId Job ID
     * @param totalCount 파싱된 총 API 개수
     * @param insertedCount 성공적으로 삽입된 API 개수
     */
    @Transactional
    public void markJobAsSuccess(Long jobId, Integer totalCount, Integer insertedCount) {
        JobEntity job = getJobById(jobId);
        job.completeWithSuccess(totalCount, insertedCount);

        log.info("Job 성공 처리 완료: jobId={}, totalCount={}, insertedCount={}, duplicateCount={}",
                jobId, totalCount, insertedCount, totalCount - insertedCount);
    }

    /**
     * Job 실패 처리
     *
     * @param jobId Job ID
     * @param errorMessage 상세 오류 메시지
     */
    @Transactional
    public void markJobAsFailure(Long jobId, String errorMessage) {
        JobEntity job = getJobById(jobId);
        job.completeWithFailure(errorMessage);

        log.error("Job 실패 처리 완료: jobId={}, errorMessage={}", jobId, errorMessage);
    }

    /**
     * 특정 서버의 진행 중인 Job 조회 (권한 검증 포함)
     *
     * @param serverId 서버 ID
     * @param currentUserId 현재 사용자 ID
     * @return JobStatusResponse (진행 중인 Job이 있으면 반환)
     * @throws com.mockops.global.exception.DomainException 진행 중인 Job이 없으면 JOB_NOT_FOUND 예외
     */
    public JobStatusResponse getActiveJobByServer(Long serverId, Long currentUserId) {
        // 권한 검증: 해당 서버가 속한 프로젝트의 DEVELOPER 이상 멤버만 조회 가능
        DomainServer server = domainServerService.getDomainServerById(serverId);
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // PROCESSING 상태인 Job 목록 조회 (동시성 제어 미흡으로 여러 개 존재 가능)
        List<JobEntity> activeJobs = jobRepository.findByServerIdAndStatusOrderBySubmittedAtDesc(serverId, JobStatus.PROCESSING);

        if (activeJobs.isEmpty()) {
            throw ErrorCode.JOB_NOT_FOUND.domainException(
                    "진행 중인 작업이 없습니다. serverId=" + serverId
            );
        }

        // 여러 개의 진행 중인 Job이 있을 경우 경고 로그
        if (activeJobs.size() > 1) {
            log.warn("[동시성 이슈] 서버에 진행 중인 Job이 {}개 존재합니다. serverId={}, jobIds={}",
                    activeJobs.size(),
                    serverId,
                    activeJobs.stream().map(JobEntity::getId).toList());
        }

        // 가장 최근 Job 반환
        JobEntity activeJob = activeJobs.get(0);
        log.info("Active Job 조회 성공: serverId={}, jobId={}, userId={}", serverId, activeJob.getId(), currentUserId);

        return JobStatusResponse.from(activeJob);
    }
}
