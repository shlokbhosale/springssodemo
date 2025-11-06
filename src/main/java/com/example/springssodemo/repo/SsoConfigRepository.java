package com.example.springssodemo.repo;

import com.example.springssodemo.model.SsoConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SsoConfigRepository extends JpaRepository<SsoConfig, Long> {
    Optional<SsoConfig> findByProviderId(String providerId);
    List<SsoConfig> findByType(String type);
}
