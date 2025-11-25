package com.mockops.global.security.oauth2;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 구현체
 * Google OAuth2 API 응답 형식:
 * {
 *   "sub": "110169484474386276334",
 *   "email": "user@example.com",
 *   "name": "John Doe",
 *   "picture": "https://lh3.googleusercontent.com/...",
 *   "given_name": "John",
 *   "family_name": "Doe",
 *   "email_verified": true
 * }
 */
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        String name = (String) attributes.get("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        // name이 없으면 email의 로컬 부분 사용
        String email = getEmail();
        return email != null ? email.split("@")[0] : "Unknown";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
