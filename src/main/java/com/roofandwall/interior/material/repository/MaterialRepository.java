package com.roofandwall.interior.material.repository;

import com.roofandwall.interior.material.model.Material;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByIdAndCompanyId(Long id, Long companyId);

    @org.springframework.data.jpa.repository.Query(
        "select m from Material m where m.companyId = :companyId and (:category = '' or m.category = :category) and (:subcategory = '' or m.subcategory = :subcategory) and (lower(m.name) like lower(concat('%', :keyword, '%')) or lower(m.brand) like lower(concat('%', :keyword, '%')))"
    )
    org.springframework.data.domain.Page<Material> search(
        Long companyId,
        String category,
        String subcategory,
        String keyword,
        org.springframework.data.domain.Pageable pageable
    );
}
