package com.barunsigong.interior.auth.repository;

import com.barunsigong.interior.auth.model.UsernameLookupAttempt;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsernameLookupAttemptRepository extends JpaRepository<UsernameLookupAttempt, Long> {
    long countByCompanyIdAndLookupKeyAndOutcomeAndCreatedAtAfter(
        Long companyId, String lookupKey, UsernameLookupAttempt.Outcome outcome, Instant after
    );

    long countByCompanyIdAndClientIpAndCreatedAtAfter(
        Long companyId, String clientIp, Instant after
    );
}
