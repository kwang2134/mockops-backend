package com.mockops.domain.project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Project 엔티티 테스트")
class ProjectTest {

    @Test
    @DisplayName("Project 객체를 생성할 수 있다")
    void createProject() {
        // given
        String name = "TestProject";
        String description = "Test Description";
        Long ownerId = 1L;
        String slackWebhookUrl = "https://hooks.slack.com/test";

        // when
        Project project = Project.builder()
                .name(name)
                .description(description)
                .ownerId(ownerId)
                .slackWebhookUrl(slackWebhookUrl)
                .build();

        // then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo(name);
        assertThat(project.getDescription()).isEqualTo(description);
        assertThat(project.getOwnerId()).isEqualTo(ownerId);
        assertThat(project.getSlackWebhookUrl()).isEqualTo(slackWebhookUrl);
    }

    @Test
    @DisplayName("프로젝트 정보를 수정할 수 있다")
    void updateProjectInfo() {
        // given
        Project project = Project.builder()
                .name("TestProject")
                .description("Old Description")
                .ownerId(1L)
                .slackWebhookUrl("https://hooks.slack.com/old")
                .build();

        String newDescription = "New Description";
        String newSlackWebhookUrl = "https://hooks.slack.com/new";

        // when
        project.updateProjectInfo(newDescription, newSlackWebhookUrl);

        // then
        assertThat(project.getDescription()).isEqualTo(newDescription);
        assertThat(project.getSlackWebhookUrl()).isEqualTo(newSlackWebhookUrl);
    }

    @Test
    @DisplayName("Slack Webhook URL 없이 프로젝트를 생성할 수 있다")
    void createProjectWithoutSlackWebhook() {
        // given
        String name = "TestProject";
        String description = "Test Description";
        Long ownerId = 1L;

        // when
        Project project = Project.builder()
                .name(name)
                .description(description)
                .ownerId(ownerId)
                .build();

        // then
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo(name);
        assertThat(project.getSlackWebhookUrl()).isNull();
    }
}
