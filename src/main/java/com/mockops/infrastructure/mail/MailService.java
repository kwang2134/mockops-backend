package com.mockops.infrastructure.mail;

import com.mockops.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

/**
 * 이메일 전송을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * HTML 이메일 발송
     *
     * @param to 수신자 이메일
     * @param subject 제목
     * @param templateName Thymeleaf 템플릿 이름
     * @param variables 템플릿 변수
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Thymeleaf 템플릿 처리
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("이메일 발송 성공: to={}, subject={}", to, subject);

        } catch (MessagingException e) {
            log.error("이메일 발송 실패: to={}, subject={}, error={}", to, subject, e.getMessage());
            throw ErrorCode.EMAIL_SEND_FAILED.serviceException();
        }
    }

    /**
     * 프로젝트 초대 이메일 발송
     *
     * @param to 수신자 이메일
     * @param inviterName 초대한 사람 이름
     * @param projectName 프로젝트 이름
     * @param invitationLink 초대 링크
     * @param expiresInDays 만료 일수
     */
    public void sendProjectInvitationEmail(String to, String inviterName, String projectName,
                                          String invitationLink, int expiresInDays) {
        Map<String, Object> variables = Map.of(
            "inviterName", inviterName,
            "projectName", projectName,
            "invitationLink", invitationLink,
            "expiresInDays", expiresInDays
        );

        String subject = "[MockOps] " + projectName + " 프로젝트에 초대되었습니다";

        sendHtmlEmail(to, subject, "project-invitation", variables);
    }
}
