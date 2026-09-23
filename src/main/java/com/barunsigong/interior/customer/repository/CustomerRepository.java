package com.barunsigong.interior.customer.repository;
import com.barunsigong.interior.customer.model.Customer;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);
    Optional<Customer> findByRequestKeyAndCompanyId(String requestKey, Long companyId);
    boolean existsByCompanyIdAndPhoneAndIdNot(Long companyId, String phone, Long id);
    long countByCompanyId(Long companyId);
    List<Customer> findByCompanyIdAndArchivedFalseOrderByName(Long companyId);
    @Query("select c from Customer c where c.companyId = :companyId and c.archived = false and (lower(c.name) like lower(concat('%', :keyword, '%')) or c.phone like concat('%', :keyword, '%'))")
    Page<Customer> search(Long companyId, String keyword, Pageable pageable);
}
