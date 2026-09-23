package com.barunsigong.interior.contract.repository;
import com.barunsigong.interior.contract.model.ConstructionContract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ConstructionContractRepository extends JpaRepository<ConstructionContract, Long> {
    Optional<ConstructionContract> findByIdAndCompanyId(Long id, Long companyId);
}
