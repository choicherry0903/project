package com.roofandwall.interior.customer.service;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public void insertCustomer(long companyId, long authorId, CustomerItem form) {
        if (registrationExists(companyId, form.getRequestKey())) return;
        long customerId = customerRepository.insertCustomer(companyId, form);
        customerRepository.insertProject(companyId, customerId, form);
        int sortOrder = 0;
        for (var item : form.getConsultationItems()) {
            if (item.getContent() == null || item.getContent().isBlank()) continue;
            customerRepository.insertConsultation(companyId, customerId, authorId, sortOrder++, item);
        }
    }

    public Optional<CustomerItem> findDetail(long companyId, long projectId) {
        return customerRepository.findDetail(companyId, projectId).map(item -> {
            item.setConsultationItems(customerRepository.findConsultationItems(companyId, item.getCustomerId()));
            return item;
        });
    }

    public List<CustomerItem.ConsultationItem> findConsultationItems(long companyId, long customerId) {
        return customerRepository.findConsultationItems(companyId, customerId);
    }

    public boolean registrationExists(long companyId, String requestKey) {
        return customerRepository.registrationExists(companyId, requestKey);
    }

    public CustomerItem requireDetail(long companyId, long projectId) {
        return findDetail(companyId, projectId).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void updateCustomer(long companyId, long projectId, long authorId, CustomerItem form) {
        var current = requireDetail(companyId, projectId);
        if (customerRepository.updateCustomer(companyId, current, form, false) != 1
                || customerRepository.updateProject(companyId, projectId, form) != 1) {
            throw new org.springframework.dao.OptimisticLockingFailureException("고객 정보가 변경되었습니다.");
        }
        customerRepository.deleteConsultationItems(companyId, current.getCustomerId());
        int order = 0;
        for (var item : form.getConsultationItems()) {
            if (item.getContent() == null || item.getContent().isBlank()) continue;
            customerRepository.insertConsultation(companyId, current.getCustomerId(), authorId, order++, item);
        }
    }

    @Transactional
    public void archiveCustomer(long companyId, long projectId, CustomerItem form) {
        var current = requireDetail(companyId, projectId);
        if (!java.util.Objects.equals(current.getProjectVersion(), form.getProjectVersion()))
            throw new org.springframework.dao.OptimisticLockingFailureException("현장 정보가 변경되었습니다.");
        current.setCustomerVersion(form.getCustomerVersion());
        if (customerRepository.updateCustomer(companyId, current, current, true) != 1)
            throw new org.springframework.dao.OptimisticLockingFailureException("고객 정보가 변경되었습니다.");
    }
}
