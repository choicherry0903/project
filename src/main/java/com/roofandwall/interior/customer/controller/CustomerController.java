package com.roofandwall.interior.customer.controller;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CustomerController {

    private final CustomerQueryService customerQueryService;

    public CustomerController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/customers";
    }

    @GetMapping("/customers")
    public String list(Model model, @AuthenticationPrincipal StaffPrincipal staff) {
        model.addAttribute("result", customerQueryService.findAll(staff.getCompanyId()));
        return "customer/customer-list";
    }

    /** 등록 화면만 제공한다. */
    @GetMapping("/customers/regform")
    public String regform(Model model, @AuthenticationPrincipal StaffPrincipal staff) {

        return "customer/customer-registration";
    }

    /** 저장 로직(등록)*/
    @PostMapping("/customers/insertCustomer")
    public String insertCustomer(Model model, @AuthenticationPrincipal StaffPrincipal staff, CustomerItem form) {
        customerQueryService.insertCustomer(form);

        return "customer/customer-list";
    }

}
