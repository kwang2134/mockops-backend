package com.mockops.domain.project.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, name = "owner_id")
    private Long ownerId;

    @Column
    private String slackWebhookUrl;

    @Builder
    public Project(String name, String description, Long ownerId, String slackWebhookUrl) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.slackWebhookUrl = slackWebhookUrl;
    }
}
