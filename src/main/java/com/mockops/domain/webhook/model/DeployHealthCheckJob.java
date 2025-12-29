package com.mockops.domain.webhook.model;

public record DeployHealthCheckJob(
        Long serverId,
        String healthcheckUrl,
        int failureCount
) {
}
