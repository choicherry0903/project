package com.barunsigong.interior.auth.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "staff_accounts")
public class StaffAccount extends BaseEntity {

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    protected StaffAccount() {}

    public StaffAccount(Long companyId, String email, String passwordHash, String displayName, Role role) {
        super(companyId);
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
