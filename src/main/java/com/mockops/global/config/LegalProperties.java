package com.mockops.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 약관 버전 설정
 * application.yml의 legal.agreements 설정을 읽어옴
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "legal.agreements")
public class LegalProperties {

    /**
     * 현재 서비스에서 요구하는 이용약관 버전
     */
    private String tosVersion;

    /**
     * 현재 서비스에서 요구하는 개인정보처리방침 버전
     */
    private String ppVersion;
}
