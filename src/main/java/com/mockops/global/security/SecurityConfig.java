package com.mockops.global.security;

import com.mockops.global.security.oauth2.CustomOAuth2UserService;
import com.mockops.global.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.mockops.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)  // CORS는 나중에 동적으로 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 인증 관련 엔드포인트는 public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // OAuth2 콜백 엔드포인트
                        .requestMatchers("/login/oauth2/**").permitAll()
                        // Mock API 엔드포인트는 public (CORS로 제어)
                        .requestMatchers("/mock/**").permitAll()
                        // Webhook 엔드포인트는 public (JWT로 제어)
                        .requestMatchers("/api/webhook/**").permitAll()
                        // Public 초대 수락 엔드포인트
                        .requestMatchers("/public/**").permitAll()
                        // 나머지는 모두 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // OAuth2 로그인 엔드포인트 설정 (Spring Security 기본값: /oauth2/authorization/{registrationId})
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/v1/auth/oauth2/authorization")
                        )
                        // OAuth2 Provider 콜백 엔드포인트 설정 (Spring Security 기본값: /login/oauth2/code/{registrationId})
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        // 커스텀 OAuth2UserService 등록
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 성공/실패 핸들러 등록
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
