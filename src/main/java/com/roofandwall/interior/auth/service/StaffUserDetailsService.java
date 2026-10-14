package com.roofandwall.interior.auth.service;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class StaffUserDetailsService implements UserDetailsService {
    private final JdbcClient jdbcClient;

    public StaffUserDetailsService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return jdbcClient.sql("""
                SELECT id, company_id, username, password_hash, display_name, role,
                       active, email, email_verified_at
                FROM interior_cms.staff_accounts
                WHERE LOWER(username) = :username
                """)
                .param("username", username.strip().toLowerCase(Locale.ROOT))
                .query((rs, rowNum) -> new StaffPrincipal(
                        rs.getLong("id"), rs.getLong("company_id"),
                        rs.getString("username"), rs.getString("password_hash"),
                        rs.getString("display_name"), rs.getString("role"),
                        rs.getBoolean("active")
                                && (rs.getString("email") == null || rs.getTimestamp("email_verified_at") != null)))
                .optional()
                .orElseThrow(() -> new UsernameNotFoundException("로그인 정보를 확인해 주세요."));
    }
}
