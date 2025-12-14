package com.mockops.domain.mock.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "mock_apis",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_mock_api_server_method_path",
            columnNames = {"server_id", "http_method", "endpoint_path"}
        )
    }
)
public class MockApi extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "server_id")
    private Long serverId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HttpMethod httpMethod;

    @Column(nullable = false)
    private String endpointPath;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public MockApi(Long serverId, String name, HttpMethod httpMethod, String endpointPath, String responseBody, Integer statusCode, Boolean isActive) {
        this.serverId = serverId;
        this.name = name;
        this.httpMethod = httpMethod;
        this.endpointPath = endpointPath;
        this.responseBody = responseBody;
        this.statusCode = statusCode != null ? statusCode : 200;
        this.isActive = isActive != null ? isActive : true;
    }

    /**
     * Mock API 정보 수정
     */
    public void updateMockApi(String name, HttpMethod httpMethod, String responseBody,
                             Integer statusCode, Boolean isActive) {
        if (name != null) {
            this.name = name;
        }
        if (httpMethod != null) {
            this.httpMethod = httpMethod;
        }
        if (responseBody != null) {
            this.responseBody = responseBody;
        }
        if (statusCode != null) {
            this.statusCode = statusCode;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    /**
     * Mock API 활성화/비활성화 토글
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * Mock API 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Mock API 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}
