package com.mockops.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthProvider 엔티티 테스트")
class AuthProviderTest {

    @Test
    @DisplayName("AuthProvider 객체를 생성할 수 있다")
    void createAuthProvider() {
        // given
        ProviderType providerType = ProviderType.GOOGLE;
        String providerId = "google_12345";
        String refreshToken = "encrypted_refresh_token";
        Long userId = 1L;

        // when
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(providerType)
                .providerId(providerId)
                .refreshToken(refreshToken)
                .userId(userId)
                .build();

        // then
        assertThat(authProvider).isNotNull();
        assertThat(authProvider.getProviderType()).isEqualTo(providerType);
        assertThat(authProvider.getProviderId()).isEqualTo(providerId);
        assertThat(authProvider.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(authProvider.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("RefreshToken을 업데이트할 수 있다")
    void updateRefreshToken() {
        // given
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.KAKAO)
                .providerId("kakao_12345")
                .refreshToken("old_token")
                .userId(1L)
                .build();

        String newToken = "new_encrypted_refresh_token";

        // when
        authProvider.updateRefreshToken(newToken);

        // then
        assertThat(authProvider.getRefreshToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("RefreshToken을 삭제할 수 있다")
    void clearRefreshToken() {
        // given
        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GITHUB)
                .providerId("github_12345")
                .refreshToken("refresh_token")
                .userId(1L)
                .build();

        // when
        authProvider.clearRefreshToken();

        // then
        assertThat(authProvider.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("여러 ProviderType으로 AuthProvider를 생성할 수 있다")
    void createAuthProviderWithDifferentProviderTypes() {
        // given & when
        AuthProvider googleProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_id")
                .userId(1L)
                .build();

        AuthProvider kakaoProvider = AuthProvider.builder()
                .providerType(ProviderType.KAKAO)
                .providerId("kakao_id")
                .userId(1L)
                .build();

        AuthProvider naverProvider = AuthProvider.builder()
                .providerType(ProviderType.NAVER)
                .providerId("naver_id")
                .userId(1L)
                .build();

        AuthProvider githubProvider = AuthProvider.builder()
                .providerType(ProviderType.GITHUB)
                .providerId("github_id")
                .userId(1L)
                .build();

        // then
        assertThat(googleProvider.getProviderType()).isEqualTo(ProviderType.GOOGLE);
        assertThat(kakaoProvider.getProviderType()).isEqualTo(ProviderType.KAKAO);
        assertThat(naverProvider.getProviderType()).isEqualTo(ProviderType.NAVER);
        assertThat(githubProvider.getProviderType()).isEqualTo(ProviderType.GITHUB);
    }
}