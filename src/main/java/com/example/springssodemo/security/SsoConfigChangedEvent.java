package com.example.springssodemo.security;

import org.springframework.context.ApplicationEvent;

/**
 * Simple event published when an SSO config is created/updated/deleted.
 * Source is the config id (Long).
 */
public class SsoConfigChangedEvent extends ApplicationEvent {
    public SsoConfigChangedEvent(Long id) {
        super(id);
    }

    public Long getConfigId() {
        return (Long) getSource();
    }
}
