package com.roofandwall.interior.customer.repository;

import com.roofandwall.interior.customer.model.Consultation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByCompanyIdAndCustomerIdOrderByCreatedAtDesc(Long companyId, Long customerId);
}
