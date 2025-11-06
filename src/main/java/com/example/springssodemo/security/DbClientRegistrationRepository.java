package com.example.springssodemo.security;

import com.example.springssodemo.model.SsoConfig;
import com.example.springssodemo.repo.SsoConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads OIDC client registrations from the database (sso_config.type == 'OIDC').
 * Uses JsonNode access to avoid casting issues.
 */
@Component
public class DbClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final SsoConfigRepository repo;
    private final SecretManager secretManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ClientRegistration> cache = new ConcurrentHashMap<>();

    public DbClientRegistrationRepository(SsoConfigRepository repo, SecretManager secretManager) {
        this.repo = repo;
        this.secretManager = secretManager;
        reload();
    }

    /**
     * Reload OIDC client registrations from DB into in-memory cache.
     */
    public synchronized void reload() {
        cache.clear();
        try {
            List<SsoConfig> oidcCfgs = repo.findByType("OIDC");
            for (SsoConfig cfg : oidcCfgs) {
                try {
                    JsonNode settings = cfg.getSettings(); // JsonNode
                    String clientId = settings != null && settings.has("clientId") ? settings.get("clientId").asText(null) : null;
                    String clientSecretPlain = null;
                    if (cfg.isHasSecret() && cfg.getSecretBlob() != null) {
                        try { clientSecretPlain = secretManager.decrypt(cfg.getSecretBlob()); }
                        catch (Exception ex) { /* fail safe: skip secret */ }
                    }
                    String issuer = settings != null && settings.has("issuer") ? settings.get("issuer").asText(null) : null;
                    String authUri = null;
                    if (settings != null && settings.has("authorizationUri")) authUri = settings.get("authorizationUri").asText(null);
                    if (authUri == null && settings != null && settings.has("authUri")) authUri = settings.get("authUri").asText(null);
                    String tokenUri = null;
                    if (settings != null && settings.has("tokenUri")) tokenUri = settings.get("tokenUri").asText(null);
                    if (tokenUri == null && settings != null && settings.has("token_endpoint")) tokenUri = settings.get("token_endpoint").asText(null);
                    String userInfoUri = settings != null && settings.has("userInfoUri") ? settings.get("userInfoUri").asText(null) : null;
                    String redirectUri = settings != null && settings.has("redirectUri") ? settings.get("redirectUri").asText("{baseUrl}/login/oauth2/code/{registrationId}") : "{baseUrl}/login/oauth2/code/{registrationId}";

                    List<String> scopes = new ArrayList<>();
                    if (settings != null && settings.has("scopes")) {
                        JsonNode scopesNode = settings.get("scopes");
                        if (scopesNode.isArray()) {
                            for (JsonNode n : scopesNode) {
                                if (!n.isNull()) scopes.add(n.asText());
                            }
                        } else {
                            String s = scopesNode.asText(null);
                            if (s != null && s.contains(",")) {
                                for (String part : s.split(",")) {
                                    String t = part.trim();
                                    if (!t.isEmpty()) scopes.add(t);
                                }
                            } else if (s != null) {
                                scopes.add(s);
                            }
                        }
                    } else {
                        // sensible defaults
                        scopes.add("openid");
                        scopes.add("profile");
                        scopes.add("email");
                    }

                    ClientRegistration.Builder b = ClientRegistration.withRegistrationId(
                                    cfg.getProviderId() != null ? cfg.getProviderId() : UUID.randomUUID().toString()
                            )
                            .clientId(clientId == null ? "" : clientId)
                            .clientSecret(clientSecretPlain == null ? "" : clientSecretPlain)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .redirectUri(redirectUri)
                            .clientName(cfg.getDisplayName() == null ? cfg.getProviderId() : cfg.getDisplayName());

                    if (issuer != null) b.issuerUri(issuer);
                    if (authUri != null) b.authorizationUri(authUri);
                    if (tokenUri != null) b.tokenUri(tokenUri);
                    if (userInfoUri != null) b.userInfoUri(userInfoUri);
                    if (!scopes.isEmpty()) b.scope(scopes.toArray(new String[0]));

                    ClientRegistration reg = b.build();
                    cache.put(cfg.getProviderId(), reg);
                } catch (Exception inner) {
                    System.err.println("Failed to build ClientRegistration for provider " + (cfg.getProviderId()) + ": " + inner.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to reload OIDC registrations: " + ex.getMessage());
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return cache.get(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return cache.values().iterator();
    }

    @EventListener
    public void onSsoConfigChanged(com.example.springssodemo.security.SsoConfigChangedEvent ev) {
        // reload cache when configs change
        reload();
    }
}
