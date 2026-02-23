package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.*;
import com.mockops.domain.user.port.TokenRefreshCachePort;
import com.mockops.domain.user.repository.AuthProviderRepository;
import com.mockops.domain.user.repository.UserAgreementRepository;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.domain.user.role.Role;
import com.mockops.global.config.LegalProperties;
import com.mockops.global.exception.ErrorCode;
import com.mockops.global.security.JwtProvider;
import com.mockops.global.util.CryptUtils;
import com.mockops.presentation.api.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final JwtProvider jwtProvider;
    private final CryptUtils cryptUtils;
    private final LegalProperties legalProperties;
    private final TokenRefreshCachePort tokenRefreshCachePort;

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {

        Optional<TokenResponse> cachedToken = tokenRefreshCachePort.getNewTokenIfInGracePeriod(refreshToken);

        if (cachedToken.isPresent()) {
            return cachedToken.get();
        }

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

        // 약관 동의 상태 조회
        List<UserAgreement> agreements = userAgreementRepository.findByUserIdOrderByAgreedAtDesc(userId);
        String tosAgreedVersion = getLatestAgreementVersion(agreements, AgreementType.TOS);
        String ppAgreedVersion = getLatestAgreementVersion(agreements, AgreementType.PP);

        // 새로운 토큰 발급 (Rotation 전략, 약관 동의 정보 포함)
        String newAccessToken = jwtProvider.generateAccessTokenWithConsent(user.getId(), tosAgreedVersion, ppAgreedVersion);
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        // 모든 AuthProvider의 RefreshToken을 새로운 것으로 업데이트
        String encryptedRefreshToken = cryptUtils.encrypt(newRefreshToken);
        authProviders.forEach(provider -> provider.updateRefreshToken(encryptedRefreshToken));

        TokenResponse newToken = new TokenResponse(newAccessToken, newRefreshToken);

        tokenRefreshCachePort.saveWithGracePeriod(refreshToken, newToken);

        return newToken;
    }

    /**
     * 특정 약관 타입의 최신 동의 버전 조회
     */
    private String getLatestAgreementVersion(List<UserAgreement> agreements, AgreementType type) {
        return agreements.stream()
                .filter(agreement -> agreement.getAgreementType() == type)
                .findFirst()
                .map(UserAgreement::getAgreementVersion)
                .orElse(null);
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
//        String accessToken = jwtProvider.generateAccessToken(user.getId());
//        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        List<UserAgreement> agreements = userAgreementRepository.findByUserIdOrderByAgreedAtDesc(user.getId());
        String tosAgreedVersion = getLatestAgreementVersion(agreements, AgreementType.TOS);
        String ppAgreedVersion = getLatestAgreementVersion(agreements, AgreementType.PP);

        // 약관 포함 버전
        String accessToken = jwtProvider.generateAccessTokenWithConsent(user.getId(), tosAgreedVersion, ppAgreedVersion);
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // RefreshToken을 AuthProvider에 암호화하여 저장
        String encryptedRefreshToken = cryptUtils.encrypt(refreshToken);
        authProvider.updateRefreshToken(encryptedRefreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }
}
