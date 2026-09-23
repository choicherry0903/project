package com.barunsigong.interior.auth.service;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class StaffAccountService implements UserDetailsService {
    private final StaffAccountRepository accounts;
    public StaffAccountService(StaffAccountRepository accounts) { this.accounts = accounts; }
    @Override public UserDetails loadUserByUsername(String email) {
        return accounts.findByEmailIgnoreCase(email.trim()).map(StaffPrincipal::new)
            .orElseThrow(() -> new UsernameNotFoundException("로그인 정보를 확인해 주세요."));
    }
}
