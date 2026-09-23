package com.barunsigong.interior.schedule.repository;

import com.barunsigong.interior.schedule.model.WorkerAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    Optional<WorkerAssignment> findByIdAndCompanyId(Long id, Long companyId);
}
