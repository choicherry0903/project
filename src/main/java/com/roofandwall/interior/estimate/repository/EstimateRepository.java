package com.roofandwall.interior.estimate.repository;

import com.roofandwall.interior.estimate.model.Estimate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    Optional<Estimate> findByIdAndCompanyId(Long id, Long companyId);
    Optional<Estimate> findByIdAndCompanyIdAndArchivedFalse(Long id, Long companyId);
    Optional<Estimate> findByCompanyIdAndRequestKey(Long companyId, String requestKey);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Estimate e where e.id = :id and e.companyId = :companyId and e.archived = false")
    Optional<Estimate> lockDraft(Long id, Long companyId);

    @org.springframework.data.jpa.repository.Query("select coalesce(max(e.revision), 0) from Estimate e where e.companyId = :companyId and e.projectId = :projectId")
    int latestRevision(Long companyId, Long projectId);

    @org.springframework.data.jpa.repository.Query("""
        select e from Estimate e, ConstructionProject p
        where e.projectId = p.id and e.companyId = p.companyId and e.companyId = :companyId
          and e.archived = false and (:projectId is null or e.projectId = :projectId)
          and (:status is null or e.status = :status)
          and lower(p.name) like lower(concat('%', :keyword, '%'))
        """)
    org.springframework.data.domain.Page<Estimate> search(Long companyId, Long projectId,
        com.roofandwall.interior.estimate.model.EstimateStatus status, String keyword,
        org.springframework.data.domain.Pageable pageable);
}
