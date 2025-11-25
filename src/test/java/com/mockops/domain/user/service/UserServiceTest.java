package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.domain.user.role.Role;
import com.mockops.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("ID로 사용자를 조회할 수 있다")
    void getUserById() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userService.getUserById(userId);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getUserByIdNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(BusinessException.class);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void getUserByEmail() {
        // given
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .nickname("testUser")
                .role(Role.USER)
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        User foundUser = userService.getUserByEmail(email);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외가 발생한다")
    void getUserByEmailNotFound() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
                .isInstanceOf(BusinessException.class);
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("사용자의 닉네임을 업데이트할 수 있다")
    void updateNickname() {
        // given
        Long userId = 1L;
        String oldNickname = "oldNickname";
        String newNickname = "newNickname";

        User user = User.builder()
                .email("test@example.com")
                .nickname(oldNickname)
                .role(Role.USER)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User updatedUser = userService.updateNickname(userId, newNickname);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 닉네임 업데이트 시 예외가 발생한다")
    void updateNicknameUserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateNickname(1L, "newNickname"))
                .isInstanceOf(BusinessException.class);
        verify(userRepository).findById(1L);
    }
}