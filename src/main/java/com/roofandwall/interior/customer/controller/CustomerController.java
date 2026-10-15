package com.roofandwall.interior.customer.controller;

import com.roofandwall.interior.customer.service.CustomerQueryService;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {

    private final CustomerQueryService customerQueryService;

    public CustomerController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/projects";
    }

    @GetMapping("/projects")
    public String list(Model model, @AuthenticationPrincipal StaffPrincipal staff) {
        model.addAttribute("projects", customerQueryService.findAll(staff.getCompanyId()));
        return "customer/customer-list";
    }

    /** 등록 화면만 제공한다. 저장 처리는 아직 구현하지 않는다. */
    @GetMapping("/projects/regform")
    public String regform(Model model, @AuthenticationPrincipal StaffPrincipal staff) {


        return "customer/customer-registration";
    }
}
