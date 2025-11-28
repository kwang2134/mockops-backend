package com.mockops.domain.project.entity;

import com.mockops.domain.common.entity.BaseEntity;
import com.mockops.domain.project.role.MemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invitations")
public class Invitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false, name = "inviter_id")
    private Long inviterId;

    @Column(nullable = false)
    private String invitedEmail;

    @Column(nullable = false, unique = true, length = 512)
    private String tokenValue;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole;

    @Builder
    public Invitation(Long projectId, Long inviterId, String invitedEmail, String tokenValue, Instant expiresAt, MemberRole memberRole) {
        this.projectId = projectId;
        this.inviterId = inviterId;
        this.invitedEmail = invitedEmail;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.status = InvitationStatus.PENDING;
        this.memberRole = memberRole != null ? memberRole : MemberRole.VIEWER;
    }

    /**
     * 초대를 수락 상태로 변경
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    /**
     * 초대를 취소 상태로 변경
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELED;
    }

    /**
     * 초대를 만료 상태로 변경
     */
    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }

    /**
     * 초대가 유효한지 확인 (PENDING 상태이고 만료 시간이 지나지 않았는지)
     */
    public boolean isValid() {
        return this.status == InvitationStatus.PENDING
            && Instant.now().isBefore(this.expiresAt);
    }
}
