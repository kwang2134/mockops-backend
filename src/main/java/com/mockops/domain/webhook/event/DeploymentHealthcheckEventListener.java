package com.mockops.domain.webhook.event;

import com.mockops.domain.webhook.port.DeployHealthCheckJobPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentHealthcheckEventListener {

    private final DeployHealthCheckJobPort deployHealthCheckJobPort;

    @Async
    @EventListener
    public void handleEvent(DeploymentHealthcheckEvent event) {
        deployHealthCheckJobPort.processDeployHealthcheck();
    }
}
