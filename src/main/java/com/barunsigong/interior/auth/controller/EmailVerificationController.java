package com.barunsigong.interior.auth.controller;

import com.barunsigong.interior.auth.service.AccountRecoveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth/verify-email")
public class EmailVerificationController {

    private final AccountRecoveryService recovery;

    public EmailVerificationController(AccountRecoveryService recovery) {
        this.recovery = recovery;
    }

    @GetMapping
    public String confirmation(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valid", recovery.canVerifyEmail(token));
        return "auth/verify-email";
    }

    @PostMapping
    public String verify(@RequestParam String token, Model model) {
        try {
            recovery.verifyEmail(token);
            model.addAttribute("success", true);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "auth/verify-email";
    }
}
