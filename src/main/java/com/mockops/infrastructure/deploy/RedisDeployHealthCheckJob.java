package com.mockops.infrastructure.deploy;

import com.mockops.domain.healthcheck.service.HealthCheckService;
import com.mockops.domain.webhook.infrastructure.DeployHealthCheckJobPort;
import com.mockops.domain.webhook.model.DeployHealthCheckJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDeployHealthCheckJob implements DeployHealthCheckJobPort {

    private static final String DEPLOY_HEALTHCHECK_KEY_PREFIX = "deploy_healthcheck:jobs";
    private final RedisTemplate<String, Object> redisTemplate;
    private final HealthCheckService healthCheckService;

    // 배포 헬스체크 작업 등록
    @Override
    public void registerDeployHealthCheck(Long domainServerId, String healthCheckUrl) {
        log.info("Deploy HealthCheck Job 등록: serverId={}", domainServerId);

        DeployHealthCheckJob job = new DeployHealthCheckJob(domainServerId, healthCheckUrl, 0);

        redisTemplate.opsForSet().add(DEPLOY_HEALTHCHECK_KEY_PREFIX, job);
    }

    // 헬스체크 수행 - 스케줄러에서 이벤트로 발행
    @Override
    public void processDeployHealthcheck() {
        Set<Object> members = redisTemplate.opsForSet().members(DEPLOY_HEALTHCHECK_KEY_PREFIX);
        if (members == null || members.isEmpty())
            return;

        // 가져온 멤버들만 제거 (동시성 이슈 방지: 키 전체 삭제 시 처리되지 않은 신규 데이터 유실 위험)
        redisTemplate.opsForSet().remove(DEPLOY_HEALTHCHECK_KEY_PREFIX, members.toArray());

        Set<DeployHealthCheckJob> jobs = new HashSet<>();
        for (Object member : members) {
            if (member instanceof DeployHealthCheckJob) {
                jobs.add((DeployHealthCheckJob) member);
            }
        }

        List<DeployHealthCheckJob> retryJobs = healthCheckService.deploymentHealthcheck(jobs);
        registerFailedJob(retryJobs);
    }

    // 실패한 헬스체크 재등록
    @Override
    public void registerFailedJob(List<DeployHealthCheckJob> retryJobs) {
        if (retryJobs.isEmpty())
            return;

        for (DeployHealthCheckJob retryJob : retryJobs) {
            redisTemplate.opsForSet().add(DEPLOY_HEALTHCHECK_KEY_PREFIX, retryJob);
        }
    }

}
