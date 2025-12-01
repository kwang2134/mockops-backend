package com.mockops.domain.mock.infrastructure;

import com.mockops.domain.mock.dto.MockCacheDto;
import com.mockops.domain.mock.entity.HttpMethod;

import java.util.Optional;

/**
 * Mock API 캐시 포트 인터페이스
 */
public interface MockApiCachePort {

    /**
     * Mock API를 캐시에 저장
     *
     * @param projectId 프로젝트 ID
     * @param serverName 서버 이름
     * @param httpMethod HTTP 메서드
     * @param endpointPath 엔드포인트 경로
     * @param cacheDto 캐시 데이터
     */
    void cacheMockApi(Long projectId, String serverName, HttpMethod httpMethod,
                     String endpointPath, MockCacheDto cacheDto);

    /**
     * 캐시에서 Mock API 조회
     *
     * @param projectId 프로젝트 ID
     * @param serverName 서버 이름
     * @param httpMethod HTTP 메서드
     * @param endpointPath 엔드포인트 경로
     * @return 캐시된 Mock API 데이터
     */
    Optional<MockCacheDto> getMockApiFromCache(Long projectId, String serverName,
                                               HttpMethod httpMethod, String endpointPath);

    /**
     * 캐시에서 Mock API 삭제
     *
     * @param projectId 프로젝트 ID
     * @param serverName 서버 이름
     * @param httpMethod HTTP 메서드
     * @param endpointPath 엔드포인트 경로
     */
    void evictMockApi(Long projectId, String serverName, HttpMethod httpMethod, String endpointPath);

    /**
     * 특정 서버의 모든 Mock API 캐시 삭제
     *
     * @param projectId 프로젝트 ID
     * @param serverName 서버 이름
     */
    void evictAllMockApisForServer(Long projectId, String serverName);
}
