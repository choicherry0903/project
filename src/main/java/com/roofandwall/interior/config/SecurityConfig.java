package com.roofandwall.interior.config;

import com.roofandwall.interior.auth.repository.StaffAccountRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, StaffAccountRepository accounts) throws Exception {
        http.authorizeHttpRequests(auth ->
            auth
                .requestMatchers("/login", "/auth/**", "/css/**", "/js/**", "/error", "/favicon.ico")
                .permitAll()
                .requestMatchers("/settings/**")
                .hasRole("ADMIN")
                .requestMatchers("/mobile/**", "/notifications/**", "/")
                .authenticated()
                .requestMatchers(
                    "/dashboard",
                    "/customers/**",
                    "/projects/**",
                    "/estimates/**",
                    "/contracts/**",
                    "/schedules/**",
                    "/work-logs/**",
                    "/materials/**",
                    "/purchase-orders/**",
                    "/costs/**",
                    "/payments/**",
                    "/completion/**",
                    "/after-service/**"
                )
                .hasAnyRole("ADMIN", "OFFICE")
                .anyRequest()
                .denyAll()
        )
            .formLogin(form ->
                form
                    .loginPage("/login")
                    .usernameParameter("username")
                    .failureUrl("/login?error")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .addFilterBefore(new ActiveAccountFilter(accounts), AuthorizationFilter.class);
        return http.build();
    }
}
