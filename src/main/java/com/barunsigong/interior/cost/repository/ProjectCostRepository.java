package com.barunsigong.interior.cost.repository;

import com.barunsigong.interior.cost.model.ProjectCost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCostRepository extends JpaRepository<ProjectCost, Long> {
    Optional<ProjectCost> findByIdAndCompanyId(Long id, Long companyId);
}
