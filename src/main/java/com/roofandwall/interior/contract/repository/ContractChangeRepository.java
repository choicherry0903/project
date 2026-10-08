package com.roofandwall.interior.contract.repository;

import com.roofandwall.interior.contract.model.ContractChange;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractChangeRepository extends JpaRepository<ContractChange, Long> {
    Optional<ContractChange> findByIdAndCompanyId(Long id, Long companyId);
}
