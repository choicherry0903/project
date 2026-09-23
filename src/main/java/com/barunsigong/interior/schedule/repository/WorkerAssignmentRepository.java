package com.barunsigong.interior.schedule.repository;
import com.barunsigong.interior.schedule.model.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    Optional<WorkerAssignment> findByIdAndCompanyId(Long id, Long companyId);
}
