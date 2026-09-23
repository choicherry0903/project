package com.barunsigong.interior.auth.service;
import com.barunsigong.interior.auth.model.StaffAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;
public class StaffPrincipal extends User {
    private final Long accountId;
    private final Long companyId;
    private final String displayName;
    public StaffPrincipal(StaffAccount account) {
        super(account.getEmail(), account.getPasswordHash(), account.isActive(), true, true, true,
            List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
        accountId = account.getId(); companyId = account.getCompanyId(); displayName = account.getDisplayName();
    }
    public Long getAccountId() { return accountId; }
    public Long getCompanyId() { return companyId; }
    public String getDisplayName() { return displayName; }
}
