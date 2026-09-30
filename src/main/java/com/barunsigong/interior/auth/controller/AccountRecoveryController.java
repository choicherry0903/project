package com.barunsigong.interior.auth.controller;

import com.barunsigong.interior.auth.service.AccountRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AccountRecoveryController {

    private final AccountRecoveryService recovery;

    public AccountRecoveryController(AccountRecoveryService recovery) {
        this.recovery = recovery;
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        try {
            boolean available = recovery.usernameAvailable(username);
            return ResponseEntity.ok(Map.of("available", available,
                "message", available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("available", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestParam String name, @RequestParam String phone,
                                                          @RequestParam String email, @RequestParam String username,
                                                          @RequestParam String password) {
        try {
            recovery.register(name, phone, email, username, password);
            return ResponseEntity.ok(Map.of("message", "인증 메일을 보냈습니다. 이메일 확인 후 로그인해 주세요."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException | org.springframework.mail.MailException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "인증 메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }

    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(@RequestParam(required = false) String name,
                                                              @RequestParam(required = false) String phone,
                                                              HttpServletRequest request) {
        var result = recovery.findUsername(name, phone, request.getRemoteAddr());
        HttpStatus status = result.limited() ? HttpStatus.TOO_MANY_REQUESTS
            : result.found() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", result.message()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestParam(required = false) String email) {
        try {
            recovery.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "인증이 필요한 계정이면 이메일을 다시 보냈습니다."));
        } catch (IllegalStateException | org.springframework.mail.MailException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "인증 메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }

}
