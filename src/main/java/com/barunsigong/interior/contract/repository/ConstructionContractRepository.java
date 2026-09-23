package com.barunsigong.interior.contract.repository;

import com.barunsigong.interior.contract.model.ConstructionContract;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructionContractRepository extends JpaRepository<ConstructionContract, Long> {
    Optional<ConstructionContract> findByIdAndCompanyId(Long id, Long companyId);
}
