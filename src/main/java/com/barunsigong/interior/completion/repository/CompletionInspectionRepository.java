package com.barunsigong.interior.completion.repository;

import com.barunsigong.interior.completion.model.CompletionInspection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionInspectionRepository extends JpaRepository<CompletionInspection, Long> {
    Optional<CompletionInspection> findByIdAndCompanyId(Long id, Long companyId);
}
