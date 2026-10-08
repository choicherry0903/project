package com.roofandwall.interior.payment.repository;

import com.roofandwall.interior.payment.model.PaymentPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    Optional<PaymentPlan> findByIdAndCompanyId(Long id, Long companyId);
}
