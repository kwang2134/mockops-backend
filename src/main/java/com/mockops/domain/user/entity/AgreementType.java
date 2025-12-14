package com.mockops.domain.user.entity;

/**
 * 약관 동의 유형
 */
public enum AgreementType {
    TOS("이용약관"),
    PP("개인정보처리방침");

    private final String description;

    AgreementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
