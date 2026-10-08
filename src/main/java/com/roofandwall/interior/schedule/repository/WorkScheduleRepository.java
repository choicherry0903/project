package com.roofandwall.interior.schedule.repository;

import com.roofandwall.interior.schedule.model.WorkSchedule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByIdAndCompanyId(Long id, Long companyId);
}
