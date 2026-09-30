package com.barunsigong.interior.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "username_lookup_attempts")
public class UsernameLookupAttempt {

    public enum Outcome { FOUND, FAILED, BLOCKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 45)
    private String clientIp;

    @Column(length = 64)
    private String lookupKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Outcome outcome;

    @Column(nullable = false)
    private Instant createdAt;

    protected UsernameLookupAttempt() {}

    public UsernameLookupAttempt(Long companyId, String clientIp, String lookupKey, Outcome outcome) {
        this.companyId = companyId;
        this.clientIp = clientIp;
        this.lookupKey = lookupKey;
        this.outcome = outcome;
        this.createdAt = Instant.now();
    }

    public Outcome getOutcome() {
        return outcome;
    }
}
