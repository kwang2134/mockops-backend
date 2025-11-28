package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.role.MemberRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserIdAndMemberRole(Long projectId, Long userId, MemberRole memberRole);

    /**
     * 커서 기반 페이징 조회 (cursorId보다 큰 ID를 가진 멤버 조회)
     */
    List<ProjectMember> findByProjectIdAndIdGreaterThanOrderByIdAsc(Long projectId, Long cursorId, Pageable pageable);

    /**
     * 커서 없이 처음부터 조회
     */
    List<ProjectMember> findByProjectIdOrderByIdAsc(Long projectId, Pageable pageable);
}
