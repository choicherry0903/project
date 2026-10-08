package com.roofandwall.interior.afterservice.repository;

import com.roofandwall.interior.afterservice.model.AfterServiceRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterServiceRequestRepository extends JpaRepository<AfterServiceRequest, Long> {
    Optional<AfterServiceRequest> findByIdAndCompanyId(Long id, Long companyId);
}
