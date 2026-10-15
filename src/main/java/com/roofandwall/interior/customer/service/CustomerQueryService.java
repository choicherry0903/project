package com.roofandwall.interior.customer.service;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerQueryService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerItem> findAll(long companyId) {
        return customerRepository.findAll(companyId);
    }

    public void insert(CustomerItem form) {

    }
}
