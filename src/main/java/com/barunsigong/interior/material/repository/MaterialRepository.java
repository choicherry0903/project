package com.barunsigong.interior.material.repository;

import com.barunsigong.interior.material.model.Material;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByIdAndCompanyId(Long id, Long companyId);
}
