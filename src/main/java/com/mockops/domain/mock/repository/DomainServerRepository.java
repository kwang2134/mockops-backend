package com.mockops.domain.mock.repository;

import com.mockops.domain.mock.entity.DomainServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainServerRepository extends JpaRepository<DomainServer, Long> {

    /**
     * 프로젝트 ID로 서버 목록 조회 (페이징)
     */
    Page<DomainServer> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 프로젝트 ID로 모든 서버 조회
     */
    List<DomainServer> findByProjectId(Long projectId);

    /**
     * 프로젝트 ID와 서버 이름으로 조회
     */
    Optional<DomainServer> findByProjectIdAndName(Long projectId, String name);

    /**
     * 프로젝트 ID와 서버 이름 존재 여부 확인
     */
    boolean existsByProjectIdAndName(Long projectId, String name);
}
