package com.roofandwall.interior.schedule.repository;

import com.roofandwall.interior.schedule.model.WorkerAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    Optional<WorkerAssignment> findByIdAndCompanyId(Long id, Long companyId);
}
