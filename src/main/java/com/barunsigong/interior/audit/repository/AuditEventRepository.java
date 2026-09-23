package com.barunsigong.interior.audit.repository;
import com.barunsigong.interior.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Optional<AuditEvent> findByIdAndCompanyId(Long id, Long companyId);
}
