package com.example.springssodemo.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Stores audit records for SSO config changes.
 */
@Entity
@Table(name = "sso_config_audit")
public class SsoConfigAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="audit_id")
    private Long auditId;

    @Column(name="sso_config_id")
    private Long ssoConfigId;

    @Column(name="changed_by")
    private String changedBy;

    @Column(name="change_type")
    private String changeType;

    @Column(columnDefinition = "jsonb")
    private String diff;

    @Column(name="created_at")
    private Instant createdAt = Instant.now();

    public SsoConfigAudit() {}

    public SsoConfigAudit(Long ssoConfigId, String changedBy, String changeType, String diff) {
        this.ssoConfigId = ssoConfigId;
        this.changedBy = changedBy;
        this.changeType = changeType;
        this.diff = diff;
        this.createdAt = Instant.now();
    }

    // getters & setters
    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public Long getSsoConfigId() { return ssoConfigId; }
    public void setSsoConfigId(Long ssoConfigId) { this.ssoConfigId = ssoConfigId; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getDiff() { return diff; }
    public void setDiff(String diff) { this.diff = diff; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
