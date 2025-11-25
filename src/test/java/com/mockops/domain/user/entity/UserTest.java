package com.mockops.domain.user.entity;

import com.mockops.domain.user.role.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User 객체를 생성할 수 있다")
    void createUser() {
        // given
        String email = "test@example.com";
        String nickname = "testUser";
        Role role = Role.USER;

        // when
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("닉네임을 변경할 수 있다")
    void updateNickname() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("oldNickname")
                .role(Role.USER)
                .build();

        String newNickname = "newNickname";

        // when
        user.updateNickname(newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("관리자 권한을 가진 User를 생성할 수 있다")
    void createAdminUser() {
        // given
        String email = "admin@example.com";
        String nickname = "adminUser";
        Role role = Role.ADMIN;

        // when
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }
}