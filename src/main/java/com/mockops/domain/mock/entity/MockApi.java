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
@Table(name = "mock_apis")
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
        this.statusCode = statusCode;
        this.isActive = isActive;
    }
}
