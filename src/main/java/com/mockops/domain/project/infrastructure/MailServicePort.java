package com.mockops.domain.project.infrastructure;

import java.util.Map;

public interface MailServicePort {

    // Thymeleaf Template를 사용한 HTML 이메일 발송
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);

    // 프로젝트 초대 이메일 발송
    public void sendProjectInvitationEmail(String to, String inviterName, String projectName, String invitationLink, int expiresInDays);
}
