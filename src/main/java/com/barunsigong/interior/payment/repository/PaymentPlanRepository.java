package com.barunsigong.interior.payment.repository;

import com.barunsigong.interior.payment.model.PaymentPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    Optional<PaymentPlan> findByIdAndCompanyId(Long id, Long companyId);
}
