package com.mockops.domain.project.repository;

import com.mockops.domain.project.entity.Invitation;
import com.mockops.domain.project.entity.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * 토큰 값으로 초대장 조회
     */
    Optional<Invitation> findByTokenValue(String tokenValue);

    /**
     * 프로젝트 ID로 초대장 목록 조회 (페이징)
     */
    Page<Invitation> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 프로젝트 ID와 상태로 초대장 목록 조회 (페이징)
     */
    Page<Invitation> findByProjectIdAndStatus(Long projectId, InvitationStatus status, Pageable pageable);

    /**
     * 프로젝트 ID와 초대 이메일, 상태로 초대장 목록 조회 (최신순)
     */
    List<Invitation> findByProjectIdAndInvitedEmailAndStatus(Long projectId, String invitedEmail, InvitationStatus status);

    /**
     * 프로젝트 ID와 초대 이메일로 PENDING 상태의 초대장이 있는지 확인
     */
    boolean existsByProjectIdAndInvitedEmailAndStatus(Long projectId, String invitedEmail, InvitationStatus status);
}
