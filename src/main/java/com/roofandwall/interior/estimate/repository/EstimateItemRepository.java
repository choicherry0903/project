package com.roofandwall.interior.estimate.repository;

import com.roofandwall.interior.estimate.model.EstimateItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateItemRepository extends JpaRepository<EstimateItem, Long> {
    Optional<EstimateItem> findByIdAndCompanyId(Long id, Long companyId);
}
