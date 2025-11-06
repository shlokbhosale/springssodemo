package com.example.springssodemo.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class SsoConfigDto {

    private Long id;
    private String providerId;
    private String type;
    private String displayName;
    private Boolean enabled;
    private Boolean hasSecret;
    private JsonNode settings;

    // Getters & Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getProviderId() { return providerId; }

    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getEnabled() { return enabled; }

    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getHasSecret() { return hasSecret; }

    public void setHasSecret(Boolean hasSecret) { this.hasSecret = hasSecret; }

    public JsonNode getSettings() { return settings; }

    public void setSettings(JsonNode settings) { this.settings = settings; }
}
