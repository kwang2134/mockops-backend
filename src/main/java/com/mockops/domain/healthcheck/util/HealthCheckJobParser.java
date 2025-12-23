package com.mockops.domain.healthcheck.util;

/**
 * 헬스 체크 작업 정보 파싱 유틸리티
 * Redis Value 형식: "{serverId}:{healthCheckPath}"
 */
public class HealthCheckJobParser {

    /**
     * Redis Value 파싱 (serverId 추출)
     *
     * @param value Redis Value (예: "123:/api/health")
     * @return 서버 ID
     */
    public static Long parseServerId(String value) {
        if (value == null || !value.contains(":")) {
            throw new IllegalArgumentException("Invalid health check job value: " + value);
        }

        String[] parts = value.split(":", 2);
        return Long.parseLong(parts[0]);
    }

    /**
     * Redis Value 파싱 (healthCheckPath 추출)
     *
     * @param value Redis Value (예: "123:/api/health")
     * @return 헬스 체크 경로
     */
    public static String parseHealthCheckPath(String value) {
        if (value == null || !value.contains(":")) {
            throw new IllegalArgumentException("Invalid health check job value: " + value);
        }

        String[] parts = value.split(":", 2);
        return parts[1];
    }
}
