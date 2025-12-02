package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.entity.ProviderType;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.AuthProviderRepository;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.domain.user.role.Role;
import com.mockops.global.exception.BusinessException;
import com.mockops.global.security.JwtProvider;
import com.mockops.global.util.CryptUtils;
import com.mockops.presentation.api.user.dto.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthProviderRepository authProviderRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CryptUtils cryptUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("유효한 RefreshToken으로 AccessToken을 갱신할 수 있다")
    void refreshAccessToken() {
        // given
        Long userId = 1L;
        String refreshToken = "valid-refresh-token";
        String encryptedRefreshToken = "encrypted-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newEncryptedRefreshToken = "new-encrypted-refresh-token";

        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();

        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_123")
                .refreshToken(encryptedRefreshToken)
                .userId(userId)
                .build();

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(authProviderRepository.findByUserId(userId)).willReturn(List.of(authProvider));
        given(cryptUtils.decrypt(encryptedRefreshToken)).willReturn(refreshToken);
        given(jwtProvider.generateAccessToken(any())).willReturn(newAccessToken);
        given(jwtProvider.generateRefreshToken(any())).willReturn(newRefreshToken);
        given(cryptUtils.encrypt(newRefreshToken)).willReturn(newEncryptedRefreshToken);

        // when
        TokenResponse response = authService.refreshAccessToken(refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
        verify(jwtProvider).validateToken(refreshToken);
        verify(userRepository).findById(userId);
        verify(authProviderRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken으로 갱신 시 예외가 발생한다")
    void refreshAccessTokenWithInvalidToken() {
        // given
        String invalidRefreshToken = "invalid-refresh-token";
        given(jwtProvider.validateToken(invalidRefreshToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(invalidRefreshToken))
                .isInstanceOf(BusinessException.class);
        verify(jwtProvider).validateToken(invalidRefreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 RefreshToken으로 갱신 시 예외가 발생한다")
    void refreshAccessTokenWithNonExistentUser() {
        // given
        Long userId = 999L;
        String refreshToken = "valid-refresh-token";

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(BusinessException.class);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("DB에 저장된 RefreshToken과 일치하지 않으면 예외가 발생한다")
    void refreshAccessTokenWithMismatchedToken() {
        // given
        Long userId = 1L;
        String refreshToken = "valid-refresh-token";
        String encryptedRefreshToken = "encrypted-refresh-token";
        String differentToken = "different-refresh-token";

        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .role(Role.USER)
                .build();

        AuthProvider authProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_123")
                .refreshToken(encryptedRefreshToken)
                .userId(userId)
                .build();

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(authProviderRepository.findByUserId(userId)).willReturn(List.of(authProvider));
        given(cryptUtils.decrypt(encryptedRefreshToken)).willReturn(differentToken);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그아웃 시 모든 AuthProvider의 RefreshToken이 제거된다")
    void logout() {
        // given
        Long userId = 1L;

        AuthProvider googleProvider = AuthProvider.builder()
                .providerType(ProviderType.GOOGLE)
                .providerId("google_123")
                .refreshToken("encrypted-token-1")
                .userId(userId)
                .build();

        AuthProvider kakaoProvider = AuthProvider.builder()
                .providerType(ProviderType.KAKAO)
                .providerId("kakao_123")
                .refreshToken("encrypted-token-2")
                .userId(userId)
                .build();

        given(authProviderRepository.findByUserId(userId)).willReturn(List.of(googleProvider, kakaoProvider));

        // when
        authService.logout(userId);

        // then
        assertThat(googleProvider.getRefreshToken()).isNull();
        assertThat(kakaoProvider.getRefreshToken()).isNull();
        verify(authProviderRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("신규 사용자 OAuth2 로그인 시 User와 AuthProvider가 생성된다")
    void handleOAuth2LoginForNewUser() {
        // given
        ProviderType providerType = ProviderType.GOOGLE;
        String providerId = "google_12345";
        String email = "newuser@example.com";
        String nickname = "newUser";
        String accessToken = "new-access-token";
        String refreshToken = "new-refresh-token";
        String encryptedRefreshToken = "encrypted-refresh-token";

        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        given(authProviderRepository.findByProviderTypeAndProviderId(providerType, providerId))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(authProviderRepository.save(any(AuthProvider.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtProvider.generateAccessToken(any())).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(any())).willReturn(refreshToken);
        given(cryptUtils.encrypt(refreshToken)).willReturn(encryptedRefreshToken);

        // when
        TokenResponse response = authService.handleOAuth2Login(providerType, providerId, email, nickname);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(userRepository).save(any(User.class));
        verify(authProviderRepository).save(any(AuthProvider.class));
    }

    @Test
    @DisplayName("기존 사용자 OAuth2 로그인 시 토큰만 발급된다")
    void handleOAuth2LoginForExistingUser() {
        // given
        Long userId = 1L;
        ProviderType providerType = ProviderType.KAKAO;
        String providerId = "kakao_12345";
        String email = "existing@example.com";
        String nickname = "existingUser";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String encryptedRefreshToken = "encrypted-refresh-token";

        User existingUser = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        AuthProvider existingAuthProvider = AuthProvider.builder()
                .providerType(providerType)
                .providerId(providerId)
                .userId(userId)
                .build();

        given(authProviderRepository.findByProviderTypeAndProviderId(providerType, providerId))
                .willReturn(Optional.of(existingAuthProvider));
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(jwtProvider.generateAccessToken(any())).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(any())).willReturn(refreshToken);
        given(cryptUtils.encrypt(refreshToken)).willReturn(encryptedRefreshToken);

        // when
        TokenResponse response = authService.handleOAuth2Login(providerType, providerId, email, nickname);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(userRepository, never()).save(any(User.class));
        verify(authProviderRepository, never()).save(any(AuthProvider.class));
    }

    @Test
    @DisplayName("기존 이메일로 다른 Provider 연결 시 AuthProvider만 추가된다")
    void handleOAuth2LoginForExistingEmailWithNewProvider() {
        // given
        ProviderType newProviderType = ProviderType.GITHUB;
        String providerId = "github_12345";
        String email = "existing@example.com";
        String nickname = "existingUser";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String encryptedRefreshToken = "encrypted-refresh-token";

        User existingUser = User.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        given(authProviderRepository.findByProviderTypeAndProviderId(newProviderType, providerId))
                .willReturn(Optional.empty());
        given(userRepository.findByEmail(email)).willReturn(Optional.of(existingUser));
        given(authProviderRepository.save(any(AuthProvider.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtProvider.generateAccessToken(any())).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(any())).willReturn(refreshToken);
        given(cryptUtils.encrypt(refreshToken)).willReturn(encryptedRefreshToken);

        // when
        TokenResponse response = authService.handleOAuth2Login(newProviderType, providerId, email, nickname);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(userRepository, never()).save(any(User.class)); // 기존 사용자 사용
        verify(authProviderRepository).save(any(AuthProvider.class)); // 새 Provider만 저장
    }
}