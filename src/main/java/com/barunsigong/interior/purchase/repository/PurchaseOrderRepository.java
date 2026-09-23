package com.barunsigong.interior.purchase.repository;
import com.barunsigong.interior.purchase.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndCompanyId(Long id, Long companyId);
}
