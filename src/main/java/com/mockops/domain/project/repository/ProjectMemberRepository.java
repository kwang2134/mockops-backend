package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.ProjectMember;
import com.mockops.domain.project.role.MemberRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * @deprecated 권한 순서 정렬을 사용하세요
     */
    @Deprecated
    List<ProjectMember> findByProjectIdAndIdGreaterThanOrderByIdAsc(Long projectId, Long cursorId, Pageable pageable);

    /**
     * 커서 없이 처음부터 조회
     * @deprecated 권한 순서 정렬을 사용하세요
     */
    @Deprecated
    List<ProjectMember> findByProjectIdOrderByIdAsc(Long projectId, Pageable pageable);

    /**
     * 권한 순서로 정렬된 커서 기반 페이징 조회
     * OWNER -> MANAGER -> DEVELOPER -> VIEWER 순서
     * 같은 권한 내에서는 ID 오름차순
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.id > :cursorId " +
           "ORDER BY CASE pm.memberRole " +
           "WHEN com.mockops.domain.project.role.MemberRole.OWNER THEN 0 " +
           "WHEN com.mockops.domain.project.role.MemberRole.MANAGER THEN 1 " +
           "WHEN com.mockops.domain.project.role.MemberRole.DEVELOPER THEN 2 " +
           "WHEN com.mockops.domain.project.role.MemberRole.VIEWER THEN 3 END, pm.id ASC")
    List<ProjectMember> findByProjectIdAndIdGreaterThanOrderByMemberRoleAscIdAsc(
            @Param("projectId") Long projectId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * 권한 순서로 정렬하여 처음부터 조회
     * OWNER -> MANAGER -> DEVELOPER -> VIEWER 순서
     * 같은 권한 내에서는 ID 오름차순
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId " +
           "ORDER BY CASE pm.memberRole " +
           "WHEN com.mockops.domain.project.role.MemberRole.OWNER THEN 0 " +
           "WHEN com.mockops.domain.project.role.MemberRole.MANAGER THEN 1 " +
           "WHEN com.mockops.domain.project.role.MemberRole.DEVELOPER THEN 2 " +
           "WHEN com.mockops.domain.project.role.MemberRole.VIEWER THEN 3 END, pm.id ASC")
    List<ProjectMember> findByProjectIdOrderByMemberRoleAscIdAsc(
            @Param("projectId") Long projectId,
            Pageable pageable
    );

    /**
     * 권한 순서로 정렬하여 처음부터 조회 (특정 사용자 제외)
     * OWNER -> MANAGER -> DEVELOPER -> VIEWER 순서
     * 같은 권한 내에서는 ID 오름차순
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.userId <> :excludeUserId " +
           "ORDER BY CASE pm.memberRole " +
           "WHEN com.mockops.domain.project.role.MemberRole.OWNER THEN 0 " +
           "WHEN com.mockops.domain.project.role.MemberRole.MANAGER THEN 1 " +
           "WHEN com.mockops.domain.project.role.MemberRole.DEVELOPER THEN 2 " +
           "WHEN com.mockops.domain.project.role.MemberRole.VIEWER THEN 3 END, pm.id ASC")
    List<ProjectMember> findByProjectIdExcludingUserOrderByMemberRoleAscIdAsc(
            @Param("projectId") Long projectId,
            @Param("excludeUserId") Long excludeUserId,
            Pageable pageable
    );

    /**
     * 특정 도메인 서버를 담당하는 멤버 목록 조회 (권한 순서로 정렬)
     * OWNER -> MANAGER -> DEVELOPER -> VIEWER 순서
     * 같은 권한 내에서는 ID 오름차순
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.domainServerId = :domainServerId " +
           "ORDER BY CASE pm.memberRole " +
           "WHEN com.mockops.domain.project.role.MemberRole.OWNER THEN 0 " +
           "WHEN com.mockops.domain.project.role.MemberRole.MANAGER THEN 1 " +
           "WHEN com.mockops.domain.project.role.MemberRole.DEVELOPER THEN 2 " +
           "WHEN com.mockops.domain.project.role.MemberRole.VIEWER THEN 3 END, pm.id ASC")
    List<ProjectMember> findByDomainServerIdOrderByMemberRoleAscIdAsc(
            @Param("domainServerId") Long domainServerId,
            Pageable pageable
    );
}
