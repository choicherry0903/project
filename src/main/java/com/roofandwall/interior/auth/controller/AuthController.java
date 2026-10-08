package com.roofandwall.interior.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        boolean worker = authentication
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"));
        return worker ? "redirect:/mobile/tasks" : "redirect:/dashboard";
    }
}
