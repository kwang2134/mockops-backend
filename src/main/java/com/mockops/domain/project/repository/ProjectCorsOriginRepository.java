package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.ProjectCorsOrigin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectCorsOriginRepository extends JpaRepository<ProjectCorsOrigin, Long> {

    List<ProjectCorsOrigin> findByProjectId(Long projectId);

    Optional<ProjectCorsOrigin> findByProjectIdAndOriginUrl(Long projectId, String originUrl);

    boolean existsByProjectIdAndOriginUrl(Long projectId, String originUrl);

    void deleteByProjectId(Long projectId);
}
