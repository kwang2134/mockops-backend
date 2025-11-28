package com.mockops.domain.project.infrastructure;

import java.util.Set;

/**
 * CORS Origin 캐시 포트 인터페이스
 * DDD의 Port-Adapter 패턴을 적용하여 도메인이 외부 기술(Redis)에 직접 의존하지 않도록 함
 */
public interface CorsOriginCachePort {

    /**
     * 특정 프로젝트의 허용된 CORS Origin 목록을 캐시에서 조회
     *
     * @param projectId 프로젝트 ID
     * @return 허용된 Origin URL 목록
     */
    Set<String> getAllowedOrigins(Long projectId);

    /**
     * 특정 프로젝트의 CORS Origin 목록을 캐시에 저장
     *
     * @param projectId 프로젝트 ID
     * @param origins 허용된 Origin URL 목록
     */
    void cacheAllowedOrigins(Long projectId, Set<String> origins);

    /**
     * 특정 프로젝트의 CORS Origin 캐시를 무효화
     *
     * @param projectId 프로젝트 ID
     */
    void evictCache(Long projectId);

    /**
     * 특정 Origin이 프로젝트에서 허용되는지 확인
     *
     * @param projectId 프로젝트 ID
     * @param origin 확인할 Origin URL
     * @return 허용 여부
     */
    boolean isOriginAllowed(Long projectId, String origin);
}
