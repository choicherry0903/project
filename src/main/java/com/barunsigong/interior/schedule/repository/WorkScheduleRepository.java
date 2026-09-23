package com.barunsigong.interior.schedule.repository;
import com.barunsigong.interior.schedule.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    Optional<WorkSchedule> findByIdAndCompanyId(Long id, Long companyId);
}
