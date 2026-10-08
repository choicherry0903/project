package com.roofandwall.interior.completion.repository;

import com.roofandwall.interior.completion.model.CompletionInspection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionInspectionRepository extends JpaRepository<CompletionInspection, Long> {
    Optional<CompletionInspection> findByIdAndCompanyId(Long id, Long companyId);
}
