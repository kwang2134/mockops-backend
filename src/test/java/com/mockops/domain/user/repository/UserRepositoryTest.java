package com.mockops.domain.user.repository;

import com.mockops.config.TestJacksonConfig;
import com.mockops.config.TestRedisConfig;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.role.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({TestJacksonConfig.class, TestRedisConfig.class})
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 저장할 수 있다")
    void saveUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getNickname()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
    void findByEmailNotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자를 삭제할 수 있다")
    void deleteUser() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);

        // when
        userRepository.deleteById(savedUser.getId());

        // then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }
}