package com.barunsigong.interior.work.repository;
import com.barunsigong.interior.work.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    Optional<WorkLog> findByIdAndCompanyId(Long id, Long companyId);
}
