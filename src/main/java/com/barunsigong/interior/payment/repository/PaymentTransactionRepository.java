package com.barunsigong.interior.payment.repository;
import com.barunsigong.interior.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdAndCompanyId(Long id, Long companyId);
}
