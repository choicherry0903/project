package com.barunsigong.interior.auth.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "staff_accounts")
public class StaffAccount extends BaseEntity {

    @Column(nullable = false, unique = true, length = 254)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(length = 15)
    private String phone;

    @Column(unique = true, length = 254)
    private String email;

    private Instant emailVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    protected StaffAccount() {}

    public StaffAccount(Long companyId, String username, String passwordHash, String displayName, Role role) {
        super(companyId);
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void requireEmailVerification(String email) {
        this.email = email;
        this.emailVerifiedAt = null;
    }

    public void verifyEmail() {
        this.emailVerifiedAt = Instant.now();
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
