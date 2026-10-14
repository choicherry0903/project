package com.roofandwall.interior.project.repository;

import com.roofandwall.interior.project.model.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ConstructionProjectRepository extends JpaRepository<ConstructionProject, Long> {
    Optional<ConstructionProject> findByIdAndCompanyId(Long id, Long companyId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ConstructionProject p where p.id = :id and p.companyId = :companyId")
    Optional<ConstructionProject> lockForEstimate(Long id, Long companyId);
    List<ConstructionProject> findByCompanyIdOrderByNameAsc(Long companyId);
    Optional<ConstructionProject> findByRequestKeyAndCompanyId(String requestKey, Long companyId);
    List<ConstructionProject> findByCompanyIdAndCustomerIdOrderByUpdatedAtDesc(Long companyId, Long customerId);
    long countByCompanyId(Long companyId);

    @Query(
        "select p from ConstructionProject p where p.companyId = :companyId and p.status <> com.roofandwall.interior.project.model.ProjectStatus.CANCELLED and (:status is null or p.status = :status) and (lower(p.name) like lower(concat('%', :keyword, '%')) or lower(p.address) like lower(concat('%', :keyword, '%')))"
    )
    Page<ConstructionProject> search(Long companyId, String keyword, ProjectStatus status, Pageable pageable);
}
