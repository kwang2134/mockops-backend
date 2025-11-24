package com.mockops.domain.project.entity;

import com.mockops.domain.common.entity.BaseEntity;
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

    @Builder
    public Invitation(Long projectId, Long inviterId, String invitedEmail, String tokenValue, Instant expiresAt, InvitationStatus status) {
        this.projectId = projectId;
        this.inviterId = inviterId;
        this.invitedEmail = invitedEmail;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.status = status;
    }
}
