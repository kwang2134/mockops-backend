package com.mockops.global.security.oauth2;

import com.mockops.domain.user.entity.ProviderType;
import com.mockops.global.exception.ErrorCode;

import java.util.Map;

/**
 * OAuth2UserInfo 생성 팩토리
 * Provider 타입에 따라 적절한 OAuth2UserInfo 구현체를 생성
 */
public class OAuth2UserInfoFactory {

    /**
     * Provider 타입에 따라 OAuth2UserInfo 구현체 생성
     *
     * @param providerType Provider 타입 (GOOGLE, GITHUB)
     * @param attributes   OAuth2 Provider로부터 받은 사용자 정보
     * @return OAuth2UserInfo 구현체
     * @throws com.mockops.global.exception.BusinessException 지원하지 않는 Provider일 경우
     */
    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        return switch (providerType) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case GITHUB -> new GithubOAuth2UserInfo(attributes);
            default -> throw ErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED.serviceException(
                    "지원하지 않는 OAuth2 Provider입니다. providerType=" + providerType
            );
        };
    }
}
