package com.mockops.global.security.oauth2;

import java.util.Map;

/**
 * OAuth2 Provider별 사용자 정보 추상화 인터페이스
 * Google, GitHub 등 다양한 Provider의 사용자 정보를 통일된 방식으로 처리
 */
public interface OAuth2UserInfo {

    /**
     * Provider에서 제공하는 고유 사용자 ID
     * @return Provider ID (예: Google의 sub, GitHub의 id)
     */
    String getProviderId();

    /**
     * 사용자 이메일
     * @return 이메일 주소
     */
    String getEmail();

    /**
     * 사용자 닉네임
     * @return 닉네임 또는 이름
     */
    String getNickname();

    /**
     * 원본 사용자 정보 (attributes)
     * @return OAuth2 Provider로부터 받은 전체 attributes
     */
    Map<String, Object> getAttributes();
}
