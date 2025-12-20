package com.mockops.domain.user.infrastructure;

import com.mockops.presentation.api.user.dto.TokenResponse;

import java.util.Optional;

public interface TokenRefreshCachePort {

    /**
     * 갱신된 토큰 정보를 캐시에 저장 (유예 기간 설정)
     * @param oldToken 교체되기 전의 이전 리프레시 토큰
     * @param newTokenResponse 새로 발급된 리프레시 토큰
     */
    void saveWithGracePeriod(String oldToken, TokenResponse newTokenResponse);


    /**
     * 해당 토큰이 현재 유예 기간 내에 있는지 확인하고, 있다면 새로 발급되었던 토큰을 반환
     * @param oldToken 확인하려는 이전 리프레시 토큰
     * @return 유예 기간 내에 존재한다면 새 토큰을 포함한 Optional, 없으면 empty
     */
    Optional<TokenResponse> getNewTokenIfInGracePeriod(String oldToken);
}
