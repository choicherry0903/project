package com.roofandwall.interior.common.controller;

import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalModelAdvice {

    @Value("${app.company-name}")
    private String companyName;

    @ModelAttribute
    public void common(Model model, Authentication auth) {
        model.addAttribute("companyName", companyName);
        model.addAttribute(
            "isAdmin",
            auth != null &&
                auth
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
        model.addAttribute(
            "isWorker",
            auth != null &&
                auth
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"))
        );
        model.addAttribute(
            "staffName",
            auth != null && auth.getPrincipal() instanceof StaffPrincipal p ? p.getDisplayName() : ""
        );
    }
}
