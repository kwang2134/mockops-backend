package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.entity.ProviderType;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.AuthProviderRepository;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.domain.user.role.Role;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.security.JwtProvider;
import com.mockops.global.util.CryptUtils;
import com.mockops.presentaion.api.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final JwtProvider jwtProvider;
    private final CryptUtils cryptUtils;

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw ErrorCode.INVALID_TOKEN.serviceException("Refresh Token이 유효하지 않습니다.");
        }

        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.serviceException(
                        "Refresh Token에 해당하는 사용자가 존재하지 않습니다. userId=" + userId
                ));

        // DB에 저장된 RefreshToken과 비교 (Rotation 전략이므로 하나의 Provider만 확인)
        List<AuthProvider> authProviders = authProviderRepository.findByUserId(userId);
        boolean isValidRefreshToken = authProviders.stream()
                .filter(provider -> provider.getRefreshToken() != null)
                .anyMatch(provider -> {
                    try {
                        String decryptedToken = cryptUtils.decrypt(provider.getRefreshToken());
                        return decryptedToken.equals(refreshToken);
                    } catch (Exception e) {
                        log.warn("RefreshToken 복호화 실패: providerId={}", provider.getId());
                        return false;
                    }
                });

        if (!isValidRefreshToken) {
            throw ErrorCode.INVALID_TOKEN.serviceException("저장된 Refresh Token과 일치하지 않습니다.");
        }

        // 새로운 토큰 발급 (Rotation 전략)
        String newAccessToken = jwtProvider.generateAccessToken(user.getId());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        // 모든 AuthProvider의 RefreshToken을 새로운 것으로 업데이트
        String encryptedRefreshToken = cryptUtils.encrypt(newRefreshToken);
        authProviders.forEach(provider -> provider.updateRefreshToken(encryptedRefreshToken));

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        // 해당 사용자의 모든 AuthProvider의 RefreshToken 무효화
        List<AuthProvider> authProviders = authProviderRepository.findByUserId(userId);
        authProviders.forEach(AuthProvider::clearRefreshToken);

        log.info("User {} logged out, RefreshTokens cleared for {} providers", userId, authProviders.size());
    }

    /**
     * OAuth2 로그인 처리 (Google, GitHub 등)
     * TODO: OAuth2 연동 구현 필요
     */
    @Transactional
    public TokenResponse handleOAuth2Login(ProviderType providerType, String providerId, String email, String nickname) {
        // 기존 AuthProvider 확인
        AuthProvider authProvider = authProviderRepository
                .findByProviderTypeAndProviderId(providerType, providerId)
                .orElse(null);

        User user;
        if (authProvider != null) {
            // 기존 사용자 로그인
            Long userId = authProvider.getUserId();
            user = userRepository.findById(userId)
                    .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.serviceException(
                            "AuthProvider에 연결된 사용자가 존재하지 않습니다. userId=" + userId
                    ));
        } else {
            // 신규 사용자 회원가입
            user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // 새로운 사용자 생성
                user = User.builder()
                        .email(email)
                        .nickname(nickname)
                        .role(Role.USER)
                        .build();
                user = userRepository.save(user);
            }

            // AuthProvider 생성
            authProvider = AuthProvider.builder()
                    .providerType(providerType)
                    .providerId(providerId)
                    .userId(user.getId())
                    .build();
            authProviderRepository.save(authProvider);
        }

        // 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // RefreshToken을 AuthProvider에 암호화하여 저장
        String encryptedRefreshToken = cryptUtils.encrypt(refreshToken);
        authProvider.updateRefreshToken(encryptedRefreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }
}
