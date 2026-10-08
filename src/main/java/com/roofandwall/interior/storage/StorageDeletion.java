package com.roofandwall.interior.storage;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "storage_deletions")
public class StorageDeletion {

    @Id
    @Column(name = "object_key", length = 512)
    private String objectKey;

    @Column(nullable = false)
    private Instant availableAt;

    @Column(nullable = false)
    private int attempts;

    protected StorageDeletion() {}

    public StorageDeletion(String key, Instant availableAt) {
        this.objectKey = key;
        this.availableAt = availableAt;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Instant getAvailableAt() {
        return availableAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void retryLater() {
        attempts++;
        availableAt = Instant.now().plusSeconds(Math.min(3600, 60L * attempts));
    }
}
