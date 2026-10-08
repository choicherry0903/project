package com.roofandwall.interior.purchase.repository;

import com.roofandwall.interior.purchase.model.PurchaseOrderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    Optional<PurchaseOrderItem> findByIdAndCompanyId(Long id, Long companyId);
}
