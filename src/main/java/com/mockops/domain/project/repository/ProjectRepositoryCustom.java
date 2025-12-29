package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 프로젝트 검색을 위한 Custom Repository 인터페이스
 */
public interface ProjectRepositoryCustom {

    /**
     * 프로젝트 검색 (제목, 오너 ID)
     *
     * @param name 프로젝트 제목 (부분 일치, null 가능)
     * @param ownerNickname 프로젝트 오너 닉네임 (null 가능)
     * @param pageable 페이징 정보
     * @return 검색된 프로젝트 페이지
     */
    Page<Project> searchProjects(String name, String ownerNickname, Pageable pageable);
}