package com.roofandwall.interior.payment.repository;

import com.roofandwall.interior.payment.model.PaymentTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByIdAndCompanyId(Long id, Long companyId);
}
