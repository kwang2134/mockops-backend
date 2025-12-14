package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.AgreementType;
import com.mockops.domain.user.entity.UserAgreement;
import com.mockops.domain.user.repository.UserAgreementRepository;
import com.mockops.presentation.api.user.dto.UserAgreementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 약관 동의 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAgreementService {

    private final UserAgreementRepository userAgreementRepository;
    private final UserService userService;

    @Value("${legal.agreements.tos-version}")
    private String tosVersion;

    @Value("${legal.agreements.pp-version}")
    private String ppVersion;


    /**
     * 약관 동의 처리
     * 개인정보처리방침과 이용약관에 대한 동의를 기록
     */
    @Transactional
    public List<UserAgreement> agreeToTerms(Long userId, UserAgreementRequest request) {
        // 사용자 존재 여부 확인
        userService.getUserById(userId);

        List<UserAgreement> agreements = request.agreements().stream()
                .map(item -> UserAgreement.builder()
                        .userId(userId)
                        .agreementType(item.agreementType())
                        .agreementVersion(switch (item.agreementType()) {
                            case AgreementType.TOS -> tosVersion;
                            case AgreementType.PP -> ppVersion;
                        })
                        .agreedAt(java.time.Instant.now())
                        .build())
                .toList();

        List<UserAgreement> savedAgreements = userAgreementRepository.saveAll(agreements);

        log.info("약관 동의 처리 완료: userId={}, agreementCount={}", userId, savedAgreements.size());
        return savedAgreements;
    }

    /**
     * 사용자의 모든 약관 동의 이력 조회
     */
    public List<UserAgreement> getUserAgreements(Long userId) {
        return userAgreementRepository.findByUserIdOrderByAgreedAtDesc(userId);
    }

    /**
     * 특정 약관 타입의 동의 이력 조회
     */
    public List<UserAgreement> getUserAgreementsByType(Long userId, AgreementType agreementType) {
        return userAgreementRepository.findByUserIdAndAgreementTypeOrderByAgreedAtDesc(userId, agreementType);
    }
}
