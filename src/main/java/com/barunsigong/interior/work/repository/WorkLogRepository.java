package com.barunsigong.interior.work.repository;

import com.barunsigong.interior.work.model.WorkLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    Optional<WorkLog> findByIdAndCompanyId(Long id, Long companyId);
}
