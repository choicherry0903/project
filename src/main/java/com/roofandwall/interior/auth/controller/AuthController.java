package com.roofandwall.interior.auth.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login(Principal principal) {
        return principal == null ? "auth/login" : "redirect:/customers";
    }
}
