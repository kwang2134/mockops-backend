package com.mockops.domain.webhook.scheduler;

import com.mockops.domain.webhook.event.DeploymentHealthcheckEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentEventScheduler {

    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 2000)
    public void deploymentHealthcheck() {
        eventPublisher.publishEvent(new DeploymentHealthcheckEvent());
    }
}
