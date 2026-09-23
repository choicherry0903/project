package com.barunsigong.interior.customer.service;

import com.barunsigong.interior.auth.service.CurrentStaff;
import com.barunsigong.interior.common.exception.ResourceNotFoundException;
import com.barunsigong.interior.customer.dto.CustomerForm;
import com.barunsigong.interior.customer.model.Customer;
import com.barunsigong.interior.customer.repository.CustomerRepository;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class CustomerService {

    private final CustomerRepository customers;
    private final CurrentStaff staff;

    public CustomerService(CustomerRepository customers, CurrentStaff staff) {
        this.customers = customers;
        this.staff = staff;
    }

    public Page<Customer> search(String keyword, int page) {
        return customers.search(
            staff.companyId(),
            keyword.trim(),
            PageRequest.of(Math.max(0, page), 20, Sort.by("updatedAt").descending())
        );
    }

    public List<Customer> choices() {
        return customers.findByCompanyIdAndArchivedFalseOrderByName(staff.companyId());
    }

    public Customer get(Long id) {
        return customers.findByIdAndCompanyId(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
    }

    public long count() {
        return customers.countByCompanyId(staff.companyId());
    }

    public boolean duplicate(CustomerForm form, Long id) {
        return customers.existsByCompanyIdAndPhoneAndIdNot(staff.companyId(), form.getPhone(), id == null ? -1L : id);
    }

    @Transactional
    public Customer save(Long id, CustomerForm form) {
        if (id == null) {
            var previous = customers.findByRequestKeyAndCompanyId(form.getRequestKey(), staff.companyId());
            if (previous.isPresent()) return previous.get();
        }
        if (duplicate(form, id) && !form.isAllowDuplicate()) throw new IllegalArgumentException(
            "동일 연락처가 있습니다. 기존 고객을 확인하거나 추가 등록에 동의해 주세요."
        );
        if (id == null) return customers.saveAndFlush(
            new Customer(
                staff.companyId(),
                form.getName(),
                form.getPhone(),
                form.getSource(),
                form.getMemo(),
                form.getRequestKey(),
                staff.get().getAccountId(),
                false
            )
        );
        Customer customer = get(id);
        if (customer.getVersion() != form.getVersion()) throw new IllegalArgumentException(
            "다른 사용자가 변경했습니다. 입력 내용을 복사한 뒤 새로고침해 주세요."
        );
        customer.updateDetails(form.getName(), form.getPhone(), form.getSource(), form.getMemo());
        return customers.saveAndFlush(customer);
    }
}
