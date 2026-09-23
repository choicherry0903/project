package com.barunsigong.interior.auth.repository;
import com.barunsigong.interior.auth.model.StaffAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {
    Optional<StaffAccount> findByEmailIgnoreCase(String email);
    boolean existsByCompanyId(Long companyId);
}
