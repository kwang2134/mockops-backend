package com.mockops.domain.webhook.port;

import com.mockops.domain.webhook.model.DeployHealthCheckJob;

import java.util.List;

public interface DeployHealthCheckJobPort {

    void registerDeployHealthCheck(Long domainServerId, String healthCheckUrl);

    void processDeployHealthcheck();

    void registerFailedJob(List<DeployHealthCheckJob> retryJobs);
}
