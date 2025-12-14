package com.mockops.domain.project.event;

import com.mockops.domain.project.role.MemberRole;

public record InvitationSendEvent(
        Object source, // Spring ApplicationEvent 생성자 필수 요소 (이벤트를 발생시킨 객체)
        Long invitationId, // DB에 저장된 초대장 ID (로깅 및 추적용)
        String invitedEmail, // 초대받은 사람의 이메일 주소 (메일 발송 대상)
        String inviterName, // 초대한 사람의 닉네임 (메일/알림 내용에 포함)
        String projectName, // 프로젝트 이름 (메일/알림 내용에 포함)
        String invitationLink, // 초대 수락 링크 (메일 본문에 포함)
        MemberRole memberRole, // 초대 시 부여할 역할 (초대 정보)
        Long invitedUserId, // 초대받은 사람이 기존 회원일 경우의 ID (알림 생성 대상, nullable)
        Long projectId // 초대한 프로젝트 ID
) {
}
