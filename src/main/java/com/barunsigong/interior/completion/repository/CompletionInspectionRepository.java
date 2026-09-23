package com.barunsigong.interior.completion.repository;
import com.barunsigong.interior.completion.model.CompletionInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CompletionInspectionRepository extends JpaRepository<CompletionInspection, Long> {
    Optional<CompletionInspection> findByIdAndCompanyId(Long id, Long companyId);
}
