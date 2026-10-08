package com.roofandwall.interior.audit.repository;

import com.roofandwall.interior.audit.model.AuditEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Optional<AuditEvent> findByIdAndCompanyId(Long id, Long companyId);
}
