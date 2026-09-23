package com.barunsigong.interior.estimate.repository;
import com.barunsigong.interior.estimate.model.EstimateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface EstimateItemRepository extends JpaRepository<EstimateItem, Long> {
    Optional<EstimateItem> findByIdAndCompanyId(Long id, Long companyId);
}
