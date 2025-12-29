package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {

    Optional<Project> findByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    /**
     * 프로젝트 ID 목록으로 페이징 조회
     */
    Page<Project> findByIdIn(List<Long> projectIds, Pageable pageable);
}
