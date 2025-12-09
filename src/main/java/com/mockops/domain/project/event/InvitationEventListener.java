package com.mockops.domain.project.event;

import com.mockops.domain.notification.entity.NotificationType;
import com.mockops.domain.notification.service.NotificationService;
import com.mockops.domain.project.infrastructure.MailServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationEventListener {

    private final MailServicePort mailService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    public void handleInvitationSendEvent(InvitationSendEvent event) {
        log.info("[ASYNC JOB START] 초대장 발송 및 알림 생성 시작: invitationId={}", event.invitationId());

        // 1. 이메일 발송
        try {
            mailService.sendProjectInvitationEmail(
                    event.invitedEmail(),
                    event.inviterName(),
                    event.projectName(),
                    event.invitationLink(),
                    7
            );
            log.info("비동기 초대 이메일 발송 성공: projectId={}, email={}", event.projectId(), event.invitedEmail());
        } catch (Exception e) {
            log.error("비동기 초대 이메일 발송 실패: projectId={}, email={}, error={}",
                    event.projectId(), event.invitedEmail(), e.getMessage());
        }

        // 2. 알림 생성 (기존 회원일 경우)
        if (event.invitedUserId() != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("projectId", event.projectId());
                metadata.put("projectName", event.projectName());
                metadata.put("invitationId", event.invitationId());
                metadata.put("inviterName", event.inviterName());
                String metadataJson = objectMapper.writeValueAsString(metadata);

                // 알림 생성
                notificationService.createNotification(
                        event.invitedUserId(),  // recipientUserId
                        null,  // domainServerId (null)
                        NotificationType.MEMBER_INVITATION_RECEIVED,
                        "프로젝트 초대",
                        event.inviterName() + "님이 '" + event.projectName() + "' 프로젝트에 초대했습니다.",
                        metadataJson
                );
                log.info("비동기 초대 알림 생성 성공: projectId={}, userId={}", event.projectId(), event.invitedUserId());
            } catch (Exception e) {
                log.error("비동기 초대 알림 생성 실패: projectId={}, userId={}, error={}",
                        event.projectId(), event.invitedUserId(), e.getMessage());
            }
        }

        log.info("[ASYNC JOB END] 초대장 발송 및 알림 생성 완료: invitationId={}", event.invitationId());
    }
}
