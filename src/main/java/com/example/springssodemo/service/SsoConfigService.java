package com.example.springssodemo.service;

import com.example.springssodemo.dto.SsoConfigDto;
import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.model.SsoConfigAudit;
import com.example.springssodemo.repo.SsoConfigAuditRepository;
import com.example.springssodemo.repo.SsoConfigRepository;
import com.example.springssodemo.security.SecretManager;
import com.example.springssodemo.security.SsoConfigChangedEvent;
import com.example.springssodemo.exception.NotFoundException;
import com.example.springssodemo.exception.ConflictException;
import com.example.springssodemo.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SsoConfigService - manages SSO config creation/update/delete with secret handling,
 * audit saving, and event publishing so runtime registries reload.
 */
@Service
public class SsoConfigService {

    private final SsoConfigRepository repo;
    private final SecretManager secretManager;
    private final ApplicationEventPublisher eventPublisher;
    private final SsoConfigAuditRepository auditRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public SsoConfigService(SsoConfigRepository repo,
                            SecretManager secretManager,
                            ApplicationEventPublisher eventPublisher,
                            SsoConfigAuditRepository auditRepo) {
        this.repo = repo;
        this.secretManager = secretManager;
        this.eventPublisher = eventPublisher;
        this.auditRepo = auditRepo;
    }

    public List<SsoConfig> listAll() {
        return repo.findAll();
    }

    public SsoConfig findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("sso_config_not_found"));
    }

    @Transactional
    public SsoConfig create(SsoConfigDto dto) {
        if (dto == null) throw new BadRequestException("invalid_payload");
        if (dto.getProviderId() == null || dto.getProviderId().isBlank()) throw new BadRequestException("providerId_required");

        if (repo.findByProviderId(dto.getProviderId()).isPresent()) {
            throw new ConflictException("provider_exists");
        }

        SsoConfig s = new SsoConfig();
        s.setProviderId(dto.getProviderId());
        s.setType(dto.getType());
        s.setDisplayName(dto.getDisplayName());
        ObjectNode settingsNode = dto.getSettings() == null ? mapper.createObjectNode()
                : mapper.convertValue(dto.getSettings(), ObjectNode.class);

        // handle clientSecret specially
        if (settingsNode.has("clientSecret")) {
            String secret = settingsNode.get("clientSecret").asText(null);
            if (secret != null && !secret.isBlank()) {
                s.setSecretBlob(secretManager.encrypt(secret));
                s.setHasSecret(true);
            }
            settingsNode.remove("clientSecret");
        }

        s.setSettings(settingsNode);
        SsoConfig saved = repo.save(s);

        saveAudit(saved.getId(), "CREATE", dto.getSettings());
        eventPublisher.publishEvent(new SsoConfigChangedEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public SsoConfig update(Long id, SsoConfigDto dto) {
        if (id == null) throw new BadRequestException("id_required");
        SsoConfig s = repo.findById(id).orElseThrow(() -> new NotFoundException("sso_config_not_found"));

        if (dto.getDisplayName() != null) s.setDisplayName(dto.getDisplayName());
        if (dto.getEnabled() != null) s.setEnabled(dto.getEnabled());

        ObjectNode incoming = dto.getSettings() == null ? mapper.createObjectNode()
                : mapper.convertValue(dto.getSettings(), ObjectNode.class);

        // If admin provided a clientSecret, encrypt and save; otherwise preserve existing secretBlob
        if (incoming.has("clientSecret")) {
            String secret = incoming.get("clientSecret").asText(null);
            if (secret != null && !secret.isBlank()) {
                s.setSecretBlob(secretManager.encrypt(secret));
                s.setHasSecret(true);
            }
            incoming.remove("clientSecret");
        }

        // Replace settings (choose semantics; replace here)
        s.setSettings(incoming);

        SsoConfig saved = repo.save(s);
        saveAudit(saved.getId(), "UPDATE", dto.getSettings());
        eventPublisher.publishEvent(new SsoConfigChangedEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        SsoConfig before = repo.findById(id).orElseThrow(() -> new NotFoundException("sso_config_not_found"));
        saveAudit(before.getId(), "DELETE", mapper.convertValue(before.getSettings(), Object.class));
        repo.deleteById(id);
        eventPublisher.publishEvent(new SsoConfigChangedEvent(id));
    }

    // Helper: store audit record
    private void saveAudit(Long ssoId, String changeType, Object diff) {
        try {
            String diffJson = diff == null ? null : mapper.writeValueAsString(diff);
            SsoConfigAudit audit = new SsoConfigAudit();
            audit.setSsoConfigId(ssoId);
            audit.setChangedBy(getCurrentAdmin());
            audit.setChangeType(changeType);
            audit.setDiff(diffJson);
            auditRepo.save(audit);
        } catch (Exception ex) {
            // Don't block main flow for audit failures; just log
            System.err.println("Failed to write sso_config_audit: " + ex.getMessage());
        }
    }

    private String getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return "system";
        return auth.getName();
    }

    /**
     * Convenience method used by import/export flows to audit & publish after direct repo saves.
     */
    @Transactional
    public void saveAuditAndPublish(SsoConfig s, String changeType) {
        saveAudit(s.getId(), changeType, s.getSettings());
        try {
            eventPublisher.publishEvent(new SsoConfigChangedEvent(s.getId()));
        } catch (Exception ignored) { }
    }
}
