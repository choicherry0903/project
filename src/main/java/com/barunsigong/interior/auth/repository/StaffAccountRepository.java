package com.barunsigong.interior.auth.repository;

import com.barunsigong.interior.auth.model.StaffAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {
    Optional<StaffAccount> findByUsernameIgnoreCase(String username);
    boolean existsByCompanyId(Long companyId);
}
