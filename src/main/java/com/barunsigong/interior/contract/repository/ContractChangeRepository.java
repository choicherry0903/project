package com.barunsigong.interior.contract.repository;
import com.barunsigong.interior.contract.model.ContractChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ContractChangeRepository extends JpaRepository<ContractChange, Long> {
    Optional<ContractChange> findByIdAndCompanyId(Long id, Long companyId);
}
