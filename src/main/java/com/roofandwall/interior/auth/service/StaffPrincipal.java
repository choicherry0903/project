package com.roofandwall.interior.auth.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.List;

public class StaffPrincipal extends User {
    private static final long serialVersionUID = 1L;
    private final long accountId;
    private final long companyId;
    private final String displayName;

    public StaffPrincipal(long accountId, long companyId, String username, String passwordHash,
                          String displayName, String role, boolean enabled) {
        super(username, passwordHash, enabled, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.accountId = accountId;
        this.companyId = companyId;
        this.displayName = displayName;
    }

    public long getAccountId() { return accountId; }
    public long getCompanyId() { return companyId; }
    public String getDisplayName() { return displayName; }
}
