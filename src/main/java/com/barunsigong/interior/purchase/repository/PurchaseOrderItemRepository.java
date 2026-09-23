package com.barunsigong.interior.purchase.repository;
import com.barunsigong.interior.purchase.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    Optional<PurchaseOrderItem> findByIdAndCompanyId(Long id, Long companyId);
}
