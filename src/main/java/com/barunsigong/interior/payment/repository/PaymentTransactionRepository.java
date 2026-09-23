package com.barunsigong.interior.payment.repository;

import com.barunsigong.interior.payment.model.PaymentTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdAndCompanyId(Long id, Long companyId);
}
