package com.mockops.presentation.api.user.dto;

import com.mockops.domain.user.entity.AgreementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 약관 동의 요청
 */
@Schema(description = "약관 동의 요청")
public record UserAgreementRequest(
        @Schema(description = "동의할 약관 목록", example = "[{\"agreementType\": \"TOS\", \"version\": \"TOS_20241215\"}, {\"agreementType\": \"PP\", \"version\": \"PP_20241215_v1.0\"}]")
        @NotEmpty(message = "동의할 약관 목록은 필수입니다")
        List<AgreementItem> agreements,

        @Schema(description = "초대 토큰 (약관 동의 후 초대 수락 처리용)", example = "eyJhbGciOiJIUzI1NiJ9....", nullable = true)
        String invitationToken
) {
    @Schema(description = "약관 동의 항목")
    public record AgreementItem(
            @Schema(description = "약관 타입 (TOS: 이용약관, PP: 개인정보처리방침)", example = "TOS")
            AgreementType agreementType

    ) {}
}
