package com.barunsigong.interior.auth.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
@Controller
public class AuthController {
    @GetMapping("/login") public String login() { return "auth/login"; }
    @GetMapping("/") public String home(Authentication authentication) {
        boolean worker = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"));
        return worker ? "redirect:/mobile/tasks" : "redirect:/dashboard";
    }
}
