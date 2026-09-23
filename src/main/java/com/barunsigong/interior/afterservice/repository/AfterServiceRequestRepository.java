package com.barunsigong.interior.afterservice.repository;

import com.barunsigong.interior.afterservice.model.AfterServiceRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterServiceRequestRepository extends JpaRepository<AfterServiceRequest, Long> {
    Optional<AfterServiceRequest> findByIdAndCompanyId(Long id, Long companyId);
}
