package com.barunsigong.interior.cost.repository;
import com.barunsigong.interior.cost.model.ProjectCost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ProjectCostRepository extends JpaRepository<ProjectCost, Long> {
    Optional<ProjectCost> findByIdAndCompanyId(Long id, Long companyId);
}
