package com.example.springssodemo.repo;

import com.example.springssodemo.model.SsoConfigAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SsoConfigAuditRepository extends JpaRepository<SsoConfigAudit, Long> {
}
