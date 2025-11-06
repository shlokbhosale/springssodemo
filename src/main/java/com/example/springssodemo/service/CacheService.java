package com.example.springssodemo.service;

import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.repo.SsoConfigRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small cache for SSO configs to reduce DB reads.
 * Refresh triggered by SsoConfigChangedEvent.
 */
@Service
public class CacheService {
    private final SsoConfigRepository repo;
    private final Map<Long, SsoConfig> cache = new ConcurrentHashMap<>();

    public CacheService(SsoConfigRepository repo) {
        this.repo = repo;
        reload();
    }

    public void reload() {
        cache.clear();
        repo.findAll().forEach(c -> cache.put(c.getId(), c));
    }

    public Optional<SsoConfig> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public Collection<SsoConfig> all() { return cache.values(); }
}
