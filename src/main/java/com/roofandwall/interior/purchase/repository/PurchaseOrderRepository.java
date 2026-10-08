package com.roofandwall.interior.purchase.repository;

import com.roofandwall.interior.purchase.model.PurchaseOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndCompanyId(Long id, Long companyId);
}
