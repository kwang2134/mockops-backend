package com.mockops.domain.user.repository;

import com.mockops.domain.user.entity.AuthProvider;
import com.mockops.domain.user.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

    Optional<AuthProvider> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);

    List<AuthProvider> findByUserId(Long userId);

    boolean existsByUserIdAndProviderType(Long userId, ProviderType providerType);
}
