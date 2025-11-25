package com.mockops.global.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2User를 래핑하여 userId를 포함하는 커스텀 OAuth2User
 * Spring Security OAuth2 인증 후 SecurityContext에 저장됨
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long userId;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nameAttributeKey;

    public CustomOAuth2User(
            Long userId,
            Map<String, Object> attributes,
            Collection<? extends GrantedAuthority> authorities,
            String nameAttributeKey) {
        this.userId = userId;
        this.attributes = attributes;
        this.authorities = authorities;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return attributes.get(nameAttributeKey) != null
                ? attributes.get(nameAttributeKey).toString()
                : String.valueOf(userId);
    }
}
