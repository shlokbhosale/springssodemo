package com.example.springssodemo.controller;

import com.example.springssodemo.dto.SsoConfigDto;
import com.example.springssodemo.dto.UserDto;
import com.example.springssodemo.mapper.ApiMapper;
import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.model.SsoConfigAudit;
import com.example.springssodemo.model.User;
import com.example.springssodemo.service.CacheService;
import com.example.springssodemo.service.SsoConfigService;
import com.example.springssodemo.service.UserService;
import com.example.springssodemo.security.SecretManager;
import com.example.springssodemo.repo.SsoConfigAuditRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminRestController
 *
 * Provides endpoints for:
 *  - Local User Management (CRUD)
 *  - SSO Config Management (CRUD + reveal secret)
 *  - Cached Config View
 *
 * All endpoints are ADMIN protected via @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    @Autowired private UserService userService;
    @Autowired private SsoConfigService ssoConfigService;
    @Autowired private CacheService cacheService;
    @Autowired private SecretManager secretManager;
    @Autowired private SsoConfigAuditRepository auditRepo;

    // ===========================
    //        USER MANAGEMENT
    // ===========================

    /**
     * List all users with pagination and optional search
     */
    @GetMapping("/users")
    public Page<UserDto> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> result = userService.list(q, pageable);
        return result.map(ApiMapper::toDto);
    }

    /**
     * Create a new local user (ADMIN only)
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto dto) {
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toDto(created));
    }

    /**
     * Update an existing user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        User updated = userService.update(id, dto);
        return ResponseEntity.ok(ApiMapper.toDto(updated));
    }

    /**
     * Delete a user by ID
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===========================
    //       SSO CONFIG CRUD
    // ===========================

    /**
     * List all SSO configurations
     */
    @GetMapping("/sso-config")
    public List<SsoConfigDto> listSsoConfigs() {
        return ssoConfigService.listAll().stream()
                .map(ApiMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single SSO configuration by ID
     */
    @GetMapping("/sso-config/{id}")
    public ResponseEntity<SsoConfigDto> getSsoConfig(@PathVariable Long id) {
        SsoConfig config = ssoConfigService.findById(id);
        return ResponseEntity.ok(ApiMapper.toDto(config));
    }

    /**
     * Create new SSO configuration (SAML, OIDC, or JWT)
     */
    @PostMapping("/sso-config")
    public ResponseEntity<SsoConfigDto> createSsoConfig(@Valid @RequestBody SsoConfigDto dto) {
        SsoConfig created = ssoConfigService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiMapper.toDto(created));
    }

    /**
     * Update existing SSO configuration by ID
     */
    @PutMapping("/sso-config/{id}")
    public ResponseEntity<SsoConfigDto> updateSsoConfig(@PathVariable Long id, @RequestBody SsoConfigDto dto) {
        SsoConfig updated = ssoConfigService.update(id, dto);
        return ResponseEntity.ok(ApiMapper.toDto(updated));
    }

    /**
     * Delete an SSO configuration
     */
    @DeleteMapping("/sso-config/{id}")
    public ResponseEntity<Void> deleteSsoConfig(@PathVariable Long id) {
        ssoConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===========================
    //      EXTRA ADMIN TOOLS
    // ===========================

    /**
     * Return cached SSO configurations (from CacheService)
     */
    @GetMapping("/sso-config/cache")
    public ResponseEntity<Collection<SsoConfig>> cachedConfigs() {
        return ResponseEntity.ok(cacheService.all());
    }

    /**
     * Reveal (decrypt) client secret for an SSO configuration.
     * This is a sensitive admin-only endpoint.
     */
    @GetMapping("/sso-config/{id}/reveal-secret")
    public ResponseEntity<Map<String, String>> revealSecret(@PathVariable Long id) {
        SsoConfig config = ssoConfigService.findById(id);
        if (!config.isHasSecret() || config.getSecretBlob() == null) {
            return ResponseEntity.ok(Map.of("secret", ""));
        }

        try {
            String secret = secretManager.decrypt(config.getSecretBlob());

            // Log audit record for secret reveal
            SsoConfigAudit audit = new SsoConfigAudit();
            audit.setSsoConfigId(config.getId());
            audit.setChangedBy(getCurrentAdmin());
            audit.setChangeType("REVEAL_SECRET");
            audit.setCreatedAt(Instant.now());
            auditRepo.save(audit);

            return ResponseEntity.ok(Map.of("secret", secret));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to decrypt secret"));
        }
    }

    // ===========================
    //     HELPER METHODS
    // ===========================

    private String getCurrentAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "system";
        return auth.getName();
    }
}
