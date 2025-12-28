package com.mockops.domain.project.entity;

import com.mockops.domain.common.entity.BaseEntity;
import com.mockops.domain.project.role.MemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "project_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"})
    }
)
public class ProjectMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole;

    @Column(nullable = false, length = 50)
    private String projectNickname;

    @Column(name = "domain_server_id")
    private Long domainServerId;

    @Builder
    public ProjectMember(Long projectId, Long userId, MemberRole memberRole, String projectNickname) {
        this.projectId = projectId;
        this.userId = userId;
        this.memberRole = memberRole;
        this.projectNickname = projectNickname;
        this.domainServerId = null;
    }

    public void updateRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public void updateProjectNickname(String projectNickname) {
        this.projectNickname = projectNickname;
    }

    public void updateDomainServer(Long domainServerId) {
        this.domainServerId = domainServerId;
    }

    public void clearDomainServer() {
        this.domainServerId = null;
    }
}
