package com.roofandwall.interior.auth.controller;

import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentStaffAdvice {
    @ModelAttribute("currentStaff")
    public StaffPrincipal currentStaff(@AuthenticationPrincipal StaffPrincipal staff) {
        return staff;
    }
}
