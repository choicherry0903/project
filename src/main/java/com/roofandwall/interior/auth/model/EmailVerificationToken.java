package com.roofandwall.interior.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long staffAccountId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected EmailVerificationToken() {}

    public EmailVerificationToken(Long companyId, Long staffAccountId, String tokenHash, Instant expiresAt) {
        this.companyId = companyId;
        this.staffAccountId = staffAccountId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getStaffAccountId() {
        return staffAccountId;
    }

    public boolean isUsable() {
        return usedAt == null && expiresAt.isAfter(Instant.now());
    }

    public void consume() {
        usedAt = Instant.now();
    }
}
