package com.mockops.global.security.oauth2;

import java.util.Map;

/**
 * GitHub OAuth2 사용자 정보 구현체
 * GitHub OAuth2 API 응답 형식:
 * {
 *   "id": 12345678,
 *   "login": "octocat",
 *   "name": "The Octocat",
 *   "email": "octocat@github.com",
 *   "avatar_url": "https://avatars.githubusercontent.com/u/...",
 *   "bio": "Developer",
 *   "location": "San Francisco"
 * }
 */
public class GithubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        // GitHub는 name이 없을 수 있으므로 login을 우선 사용
        String name = (String) attributes.get("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String login = (String) attributes.get("login");
        if (login != null && !login.isBlank()) {
            return login;
        }

        // name과 login 모두 없으면 email 로컬 부분 사용
        String email = getEmail();
        return email != null ? email.split("@")[0] : "Unknown";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
