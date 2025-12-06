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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    // Swagger Path
    private final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/docs/**",
    };

    /**
     * CORS 설정
     * MockOps 서비스 자체 프론트엔드만 허용
     * /mock/** 경로는 DynamicCorsFilter가 동적으로 처리
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration serviceConfig = new CorsConfiguration();

        // MockOps 프론트엔드 허용
        serviceConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        serviceConfig.setAllowedMethods(List.of("*"));
        serviceConfig.setAllowedHeaders(List.of("*"));
        serviceConfig.setAllowCredentials(true);  // 쿠키 전송 허용 (CRITICAL!)
        serviceConfig.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        serviceConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // MockOps 서비스 자체 API에만 적용 (/mock/** 제외!)
        source.registerCorsConfiguration("/api/**", serviceConfig);
        source.registerCorsConfiguration("/login/**", serviceConfig);
        source.registerCorsConfiguration("/public/**", serviceConfig);

        // /mock/** 는 DynamicCorsFilter가 프로젝트별로 동적 처리하므로 여기서 설정 안 함

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정 적용
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
                        // Swagger 엔드포인트
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        // test용 JWT 발급
                        .requestMatchers("/api/v1/test/**").permitAll()
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
