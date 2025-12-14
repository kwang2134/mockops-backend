package com.mockops.presentation.api.user.dto;

import com.mockops.domain.user.entity.AgreementType;
import com.mockops.domain.user.entity.UserAgreement;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * 약관 동의 이력 응답
 */
@Schema(description = "약관 동의 이력 응답")
public record UserAgreementResponse(
        @Schema(description = "약관 동의 이력 ID", example = "1")
        Long id,

        @Schema(description = "약관 타입 (TOS: 이용약관, PP: 개인정보처리방침)", example = "TOS")
        AgreementType agreementType,

        @Schema(description = "약관 버전", example = "TOS_20241215")
        String agreementVersion,

        @Schema(description = "동의 일시 (타임스탬프)", example = "2025-12-14T12:00:00Z")
        Instant agreedAt
) {
    public static UserAgreementResponse from(UserAgreement agreement) {
        return new UserAgreementResponse(
                agreement.getId(),
                agreement.getAgreementType(),
                agreement.getAgreementVersion(),
                agreement.getAgreedAt()
        );
    }

    public static List<UserAgreementResponse> from(List<UserAgreement> agreements) {
        return agreements.stream()
                .map(UserAgreementResponse::from)
                .toList();
    }
}
