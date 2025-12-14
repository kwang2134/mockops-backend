package com.mockops.domain.user.repository;

import com.mockops.config.TestJacksonConfig;
import com.mockops.config.TestRedisConfig;
import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.entity.ProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({TestJacksonConfig.class, TestRedisConfig.class})
@DisplayName("AuthProviderRepository 테스트")
class AuthProviderRepositoryTest {

    @Autowired
    private AuthProviderRepository authProviderRepository;

    @Test
    @DisplayName("AuthProvider를 저장할 수 있다")
    void saveAuthProvider() {
        // given
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_12345")
                .refreshToken("refresh_token")
                .userId(1L)
                .build();

        // when
        AuthProvider savedAuthProvider = authProviderRepository.save(authProvider);

        // then
        assertThat(savedAuthProvider).isNotNull();
        assertThat(savedAuthProvider.getId()).isNotNull();
        assertThat(savedAuthProvider.getProviderType()).isEqualTo(ProviderType.GOOGLE);
    }

    @Test
    @DisplayName("ProviderType과 ProviderId로 AuthProvider를 조회할 수 있다")
    void findByProviderTypeAndProviderId() {
        // given
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.KAKAO)
                .providerId("kakao_12345")
                .refreshToken("refresh_token")
                .userId(1L)
                .build();
        authProviderRepository.save(authProvider);

        // when
        Optional<AuthProvider> foundAuthProvider = authProviderRepository
                .findByProviderTypeAndProviderId(ProviderType.KAKAO, "kakao_12345");

        // then
        assertThat(foundAuthProvider).isPresent();
        assertThat(foundAuthProvider.get().getProviderType()).isEqualTo(ProviderType.KAKAO);
        assertThat(foundAuthProvider.get().getProviderId()).isEqualTo("kakao_12345");
    }

    @Test
    @DisplayName("존재하지 않는 ProviderType과 ProviderId로 조회 시 빈 Optional을 반환한다")
    void findByProviderTypeAndProviderIdNotFound() {
        // when
        Optional<AuthProvider> foundAuthProvider = authProviderRepository
                .findByProviderTypeAndProviderId(ProviderType.NAVER, "nonexistent_id");

        // then
        assertThat(foundAuthProvider).isEmpty();
    }

    @Test
    @DisplayName("UserId로 모든 AuthProvider를 조회할 수 있다")
    void findByUserId() {
        // given
        Long userId = 1L;
        AuthProvider googleProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_12345")
                .userId(userId)
                .build();

        AuthProvider kakaoProvider = AuthProvider.builder()
                .providerType(ProviderType.KAKAO)
                .providerId("kakao_12345")
                .userId(userId)
                .build();

        authProviderRepository.save(googleProvider);
        authProviderRepository.save(kakaoProvider);

        // when
        List<AuthProvider> authProviders = authProviderRepository.findByUserId(userId);

        // then
        assertThat(authProviders).hasSize(2);
        assertThat(authProviders)
                .extracting(AuthProvider::getProviderType)
                .containsExactlyInAnyOrder(ProviderType.GOOGLE, ProviderType.KAKAO);
    }

    @Test
    @DisplayName("UserId와 ProviderType으로 AuthProvider 존재 여부를 확인할 수 있다")
    void existsByUserIdAndProviderType() {
        // given
        Long userId = 1L;
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GITHUB)
                .providerId("github_12345")
                .userId(userId)
                .build();
        authProviderRepository.save(authProvider);

        // when
        boolean exists = authProviderRepository.existsByUserIdAndProviderType(userId, ProviderType.GITHUB);
        boolean notExists = authProviderRepository.existsByUserIdAndProviderType(userId, ProviderType.NAVER);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("RefreshToken을 업데이트할 수 있다")
    void updateRefreshToken() {
        // given
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_12345")
                .refreshToken("old_token")
                .userId(1L)
                .build();
        AuthProvider savedAuthProvider = authProviderRepository.save(authProvider);

        // when
        savedAuthProvider.updateRefreshToken("new_token");
        authProviderRepository.flush();

        // then
        AuthProvider updatedAuthProvider = authProviderRepository.findById(savedAuthProvider.getId()).orElseThrow();
        assertThat(updatedAuthProvider.getRefreshToken()).isEqualTo("new_token");
    }
}