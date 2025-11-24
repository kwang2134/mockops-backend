package com.mockops.domain.user.entity;

import com.mockops.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auth_providers")
public class AuthProvider extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType providerType;

    @Column(nullable = false)
    private String providerId;

    @Column(length = 512)
    private String refreshToken;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Builder
    public AuthProvider(ProviderType providerType, String providerId, String refreshToken, Long userId) {
        this.providerType = providerType;
        this.providerId = providerId;
        this.refreshToken = refreshToken;
        this.userId = userId;
    }
}
