package com.example.springssodemo.model;

import com.example.springssodemo.util.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;

@Entity
@Table(name = "sso_config")
public class SsoConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String providerId;

    private String type;
    private String displayName;

    private boolean enabled = true;
    private boolean hasSecret = false;

    @Lob
    private String secretBlob;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode settings;

    public SsoConfig() {}

    // Getters & Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getProviderId() { return providerId; }

    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isHasSecret() { return hasSecret; }

    public void setHasSecret(boolean hasSecret) { this.hasSecret = hasSecret; }

    public String getSecretBlob() { return secretBlob; }

    public void setSecretBlob(String secretBlob) { this.secretBlob = secretBlob; }

    public JsonNode getSettings() { return settings; }

    public void setSettings(JsonNode settings) { this.settings = settings; }
}
