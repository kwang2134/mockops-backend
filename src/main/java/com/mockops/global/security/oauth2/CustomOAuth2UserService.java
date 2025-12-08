package com.mockops.global.security.oauth2;

import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.entity.ProviderType;
import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.AuthProviderRepository;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.domain.user.role.Role;
import com.mockops.global.exception.BusinessException;
import com.mockops.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OAuth2 사용자 정보를 로드하고 DB에 저장/업데이트하는 서비스
 * Spring Security OAuth2가 Provider로부터 사용자 정보를 받은 후 호출됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    /**
     * OAuth2 Provider로부터 사용자 정보를 로드
     * 기존 사용자는 로그인 처리, 신규 사용자는 자동 회원가입
     *
     * @param userRequest OAuth2UserRequest (Provider 정보, Access Token 포함)
     * @return OAuth2User (인증된 사용자 정보)
     * @throws OAuth2AuthenticationException OAuth2 인증 실패 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 상위 클래스의 loadUser로 OAuth2User 정보 가져오기
            OAuth2User oAuth2User = super.loadUser(userRequest);

            // Provider 정보 추출
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            ProviderType providerType = getProviderType(registrationId);

            // OAuth2UserInfo로 변환
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                    providerType,
                    oAuth2User.getAttributes()
            );

            // 사용자 정보 검증
            if (oAuth2UserInfo.getProviderId() == null || oAuth2UserInfo.getProviderId().isBlank()) {
                throw ErrorCode.OAUTH2_USER_INFO_ERROR.serviceException(
                        "Provider ID를 가져올 수 없습니다. registrationId=" + registrationId
                );
            }

            // 이메일 검증 및 GitHub의 경우 추가 API 호출로 이메일 가져오기
            String email = oAuth2UserInfo.getEmail();
            if (email == null || email.isBlank()) {
                // GitHub의 경우 이메일이 public이 아니면 /user/emails API 호출
                if (providerType == ProviderType.GITHUB) {
                    log.warn("[GitHub OAuth] 이메일이 public이 아닙니다. /user/emails API를 호출합니다.");
                    email = fetchGithubPrimaryEmail(userRequest.getAccessToken().getTokenValue());

                    if (email == null || email.isBlank()) {
                        log.error("GitHub /user/emails API에서도 이메일을 가져올 수 없음");
                        throw ErrorCode.OAUTH2_USER_INFO_ERROR.serviceException(
                                "GitHub에서 이메일을 가져올 수 없습니다. 이메일 권한을 확인해주세요."
                        );
                    }

                    log.info("[GitHub OAuth] /user/emails API로부터 이메일 획득: {}", email);

                    // 이메일을 attributes에 추가 (OAuth2UserInfo에서 사용하기 위해)
                    Map<String, Object> modifiedAttributes = new java.util.HashMap<>(oAuth2User.getAttributes());
                    modifiedAttributes.put("email", email);
                    oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, modifiedAttributes);
                } else {
                    log.error("이메일을 가져올 수 없음: Attributes {}", oAuth2User.getAttributes());
                    throw ErrorCode.OAUTH2_USER_INFO_ERROR.serviceException(
                            "이메일을 가져올 수 없습니다. registrationId=" + registrationId
                    );
                }
            }

            // 사용자 조회 또는 생성
            User user = processOAuth2User(providerType, oAuth2UserInfo);

            // CustomOAuth2User로 래핑하여 반환 (userId 포함)
            return new CustomOAuth2User(
                    user.getId(),
                    oAuth2User.getAttributes(),
                    oAuth2User.getAuthorities(),
                    "email" // nameAttributeKey (Provider마다 다를 수 있음)
            );

        } catch (BusinessException e) {
            // 커스텀 예외는 메시지만 포함하여 OAuth2AuthenticationException으로 변환
            log.error("[FATAL] Business Exception during OAuth2 load: {}", e.getMessage());
            throw new OAuth2AuthenticationException(e.getMessage());
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 로드 실패", e);
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }

    /**
     * 사용자 조회 또는 생성
     * 기존 AuthProvider가 있으면 해당 사용자 반환
     * 없으면 신규 사용자 생성 및 AuthProvider 생성
     *
     * @param providerType    Provider 타입
     * @param oAuth2UserInfo  OAuth2 사용자 정보
     * @return User 엔티티
     */
    private User processOAuth2User(ProviderType providerType, OAuth2UserInfo oAuth2UserInfo) {
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String nickname = oAuth2UserInfo.getNickname();

        // 기존 AuthProvider 확인
        AuthProvider authProvider = authProviderRepository
                .findByProviderTypeAndProviderId(providerType, providerId)
                .orElse(null);

        if (authProvider != null) {
            // 기존 사용자 로그인
            Long userId = authProvider.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.serviceException(
                            "AuthProvider에 연결된 사용자가 존재하지 않습니다. userId=" + userId
                    ));

            log.info("기존 사용자 로그인: userId={}, providerType={}", userId, providerType);
            return user;
        }

        // 신규 사용자 처리
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 완전히 새로운 사용자 생성
            user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .role(Role.USER)
                    .build();
            user = userRepository.save(user);
            log.info("신규 사용자 생성: userId={}, email={}, providerType={}", user.getId(), email, providerType);
        } else {
            log.info("기존 이메일로 OAuth2 연동: userId={}, email={}, providerType={}", user.getId(), email, providerType);
        }

        // AuthProvider 생성
        authProvider = AuthProvider.builder()
                .providerType(providerType)
                .providerId(providerId)
                .userId(user.getId())
                .build();
        authProviderRepository.save(authProvider);

        log.info("AuthProvider 생성 완료: userId={}, providerType={}, providerId={}", user.getId(), providerType, providerId);

        return user;
    }

    /**
     * registrationId를 ProviderType으로 변환
     *
     * @param registrationId Spring Security OAuth2 Client Registration ID (google, github 등)
     * @return ProviderType
     */
    private ProviderType getProviderType(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ProviderType.GOOGLE;
            case "github" -> ProviderType.GITHUB;
            default -> throw ErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED.serviceException(
                    "지원하지 않는 OAuth2 Provider입니다. registrationId=" + registrationId
            );
        };
    }

    /**
     * GitHub /user/emails API를 호출하여 primary 이메일 가져오기
     * GitHub 사용자가 이메일을 public으로 설정하지 않은 경우에 사용
     *
     * API 응답 예시:
     * [
     *   {
     *     "email": "octocat@github.com",
     *     "primary": true,
     *     "verified": true,
     *     "visibility": "private"
     *   }
     * ]
     *
     * @param accessToken GitHub OAuth2 Access Token
     * @return Primary 이메일 주소 (없으면 null)
     */
    private String fetchGithubPrimaryEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.github.com/user/emails";

            // HTTP 헤더 설정 (Authorization, Accept)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GitHub API 호출
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> emails = response.getBody();
            if (emails == null || emails.isEmpty()) {
                log.warn("[GitHub OAuth] /user/emails API 응답이 비어있습니다.");
                return null;
            }

            // primary 이메일 찾기
            for (Map<String, Object> emailInfo : emails) {
                Boolean isPrimary = (Boolean) emailInfo.get("primary");
                if (isPrimary != null && isPrimary) {
                    String email = (String) emailInfo.get("email");
                    log.info("[GitHub OAuth] Primary 이메일 발견: {}", email);
                    return email;
                }
            }

            // primary가 없으면 첫 번째 이메일 반환
            String firstEmail = (String) emails.get(0).get("email");
            log.info("[GitHub OAuth] Primary 이메일이 없어 첫 번째 이메일 사용: {}", firstEmail);
            return firstEmail;

        } catch (Exception e) {
            log.error("[GitHub OAuth] /user/emails API 호출 실패", e);
            return null;
        }
    }
}
