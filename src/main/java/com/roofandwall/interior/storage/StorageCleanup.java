package com.roofandwall.interior.storage;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class StorageCleanup {

    private final StorageDeletionRepository deletions;

    public StorageCleanup(StorageDeletionRepository deletions) {
        this.deletions = deletions;
    }

    /** Write ahead: an interrupted upload or rolled-back DB transaction stays queued. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepareUpload(String key) {
        deletions.saveAndFlush(new StorageDeletion(key, Instant.now().plusSeconds(3600)));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void retainUpload(String key) {
        deletions.deleteById(key);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteAfterCommit(String key) {
        if (key != null) deletions.save(new StorageDeletion(key, Instant.now()));
    }
}
