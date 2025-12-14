package com.mockops.domain.user.repository;

import com.mockops.domain.user.entity.AgreementType;
import com.mockops.domain.user.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    /**
     * 특정 사용자의 모든 약관 동의 이력 조회
     */
    List<UserAgreement> findByUserIdOrderByAgreedAtDesc(Long userId);

    /**
     * 특정 사용자의 특정 약관 타입 동의 이력 조회
     */
    List<UserAgreement> findByUserIdAndAgreementTypeOrderByAgreedAtDesc(Long userId, AgreementType agreementType);
}
