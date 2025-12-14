package com.mockops.domain.user.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자 약관 동의 히스토리
 * 개인정보처리방침 및 이용약관 동의 이력을 관리
 * 법적 요구사항인 기록 보존 의무와 약관 버전별 추적을 위한 테이블
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_agreement_logs")
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 50)
    private AgreementType agreementType;

    @Column(name = "agreement_version", nullable = false, length = 50)
    private String agreementVersion;

    @Column(name = "agreed_at", nullable = false)
    private Instant agreedAt;

    @Builder
    public UserAgreement(Long userId, AgreementType agreementType, String agreementVersion, Instant agreedAt) {
        this.userId = userId;
        this.agreementType = agreementType;
        this.agreementVersion = agreementVersion;
        this.agreedAt = agreedAt;
    }
}
