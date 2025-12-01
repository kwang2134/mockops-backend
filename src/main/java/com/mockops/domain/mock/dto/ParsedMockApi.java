package com.mockops.domain.mock.dto;

import com.mockops.domain.mock.entity.HttpMethod;
import lombok.Builder;
import lombok.Getter;

/**
 * OpenAPI 스펙 파일에서 파싱된 Mock API 정보
 */
@Getter
@Builder
public class ParsedMockApi {

    /**
     * API 이름 (tags 또는 summary에서 추출)
     */
    private String name;

    /**
     * HTTP 메서드
     */
    private HttpMethod httpMethod;

    /**
     * 엔드포인트 경로
     */
    private String endpointPath;

    /**
     * 응답 본문 (JSON 문자열)
     */
    private String responseBody;

    /**
     * HTTP 상태 코드
     */
    private Integer statusCode;

    /**
     * 활성화 여부 (기본값 true)
     */
    private Boolean isActive;
}