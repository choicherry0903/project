package com.roofandwall.interior.auth.repository;

import com.roofandwall.interior.auth.model.EmailVerificationToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    List<EmailVerificationToken> findByCompanyIdAndStaffAccountIdAndUsedAtIsNull(Long companyId, Long staffAccountId);
    long countByCompanyIdAndStaffAccountIdAndCreatedAtAfter(Long companyId, Long staffAccountId, Instant after);
}
