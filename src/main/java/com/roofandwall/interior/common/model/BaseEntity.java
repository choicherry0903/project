package com.roofandwall.interior.common.model;

import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long companyId;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected BaseEntity() {}

    protected BaseEntity(Long companyId) {
        this.companyId = companyId;
    }

    @PrePersist
    void createTimestamp() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
