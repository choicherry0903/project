package com.barunsigong.interior.estimate.repository;

import com.barunsigong.interior.estimate.model.Estimate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    Optional<Estimate> findByIdAndCompanyId(Long id, Long companyId);
}
