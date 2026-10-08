package com.roofandwall.interior.auth.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentStaff {

    public StaffPrincipal get() {
        return (StaffPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Long companyId() {
        return get().getCompanyId();
    }
}
