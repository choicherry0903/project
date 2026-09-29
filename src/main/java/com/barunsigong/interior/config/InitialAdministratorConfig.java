package com.barunsigong.interior.config;

import com.barunsigong.interior.auth.model.Role;
import com.barunsigong.interior.auth.model.StaffAccount;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialAdministratorConfig {

    @Bean
    CommandLineRunner initializeAdministrator(
        StaffAccountRepository accounts,
        PasswordEncoder encoder,
        @Value("${app.company-id}") Long companyId,
        @Value("${app.bootstrap.username}") String username,
        @Value("${app.bootstrap.password}") String password
    ) {
        return args -> {
            if (accounts.existsByCompanyId(companyId)) return;
            if (username.isBlank() || password.length() < 12) {
                throw new IllegalStateException("최초 실행에는 ADMIN_USERNAME과 12자 이상의 ADMIN_PASSWORD가 필요합니다.");
            }
            accounts.save(
                new StaffAccount(
                    companyId,
                    username.trim().toLowerCase(Locale.ROOT),
                    encoder.encode(password),
                    "관리자",
                    Role.ADMIN
                )
            );
        };
    }
}
