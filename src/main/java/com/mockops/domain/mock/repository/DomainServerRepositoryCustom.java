package com.mockops.domain.mock.repository;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 도메인 서버 검색을 위한 Custom Repository 인터페이스
 */
public interface DomainServerRepositoryCustom {

    /**
     * 도메인 서버 검색 (이름, 상태)
     *
     * @param projectId 프로젝트 ID (필수)
     * @param name 도메인 서버 이름 (부분 일치, null 가능)
     * @param status 서버 상태 (null 가능)
     * @param pageable 페이징 정보
     * @return 검색된 도메인 서버 페이지
     */
    Page<DomainServer> searchDomainServers(Long projectId, String name, ServerStatus status, Pageable pageable);
}