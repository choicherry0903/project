package com.barunsigong.interior.payment.repository;
import com.barunsigong.interior.payment.model.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    Optional<PaymentPlan> findByIdAndCompanyId(Long id, Long companyId);
}
