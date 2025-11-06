package com.example.springssodemo.security;

import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.repo.SsoConfigRepository;
import com.example.springssodemo.security.SecretManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically loads SAML RelyingPartyRegistration definitions from the DB.
 */
@Component
public class DbRelyingPartyRegistrationRepository implements RelyingPartyRegistrationRepository, Iterable<RelyingPartyRegistration> {

    private final SsoConfigRepository repo;
    private final SecretManager secretManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, RelyingPartyRegistration> cache = new ConcurrentHashMap<>();

    public DbRelyingPartyRegistrationRepository(SsoConfigRepository repo, SecretManager secretManager) {
        this.repo = repo;
        this.secretManager = secretManager;
        reload();
    }

    @SuppressWarnings("unchecked")
    public synchronized void reload() {
        cache.clear();
        List<SsoConfig> samlConfigs = repo.findByType("SAML");

        for (SsoConfig cfg : samlConfigs) {
            try {
                JsonNode settings = cfg.getSettings();
                Map<String, Object> map = mapper.convertValue(settings, Map.class);

                String entityId = (String) map.getOrDefault("entityId", cfg.getProviderId());
                String ssoUrl = (String) map.getOrDefault("ssoUrl", "");
                String sloUrl = (String) map.getOrDefault("sloUrl", "");
                String cert = (String) map.getOrDefault("certificate", "");

                RelyingPartyRegistration.Builder builder = RelyingPartyRegistration
                        .withRegistrationId(cfg.getProviderId())
                        .entityId(entityId)
                        .assertingPartyDetails(p -> p
                                .entityId(entityId)
                                .singleSignOnServiceLocation(ssoUrl)
                                .singleLogoutServiceLocation(sloUrl)
                                .wantAuthnRequestsSigned(false)
                                .verificationX509Credentials(c -> {})
                        );

                RelyingPartyRegistration registration = builder.build();
                cache.put(cfg.getProviderId(), registration);

            } catch (Exception e) {
                System.err.println("Failed to parse SAML config for provider " + cfg.getProviderId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public RelyingPartyRegistration findByRegistrationId(String id) {
        return cache.get(id);
    }

    @Override
    public Iterator<RelyingPartyRegistration> iterator() {
        return cache.values().iterator();
    }

    @EventListener
    public void onSsoConfigChanged(SsoConfigChangedEvent ev) {
        reload();
    }
}
