package com.barunsigong.interior.storage;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.storage.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class StorageCleanupWorker {

    private static final Logger LOG = LoggerFactory.getLogger(StorageCleanupWorker.class);
    private final StorageDeletionRepository deletions;
    private final ImageStorage storage;

    public StorageCleanupWorker(StorageDeletionRepository deletions, ImageStorage storage) {
        this.deletions = deletions;
        this.storage = storage;
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    @Transactional
    public void clean() {
        if (!storage.configured()) return;
        for (var deletion : deletions.findTop20ByAvailableAtLessThanEqualOrderByAvailableAtAsc(Instant.now())) {
            try {
                storage.delete(deletion.getObjectKey());
                deletions.delete(deletion);
            } catch (ImageStorageException e) {
                deletion.retryLater();
                LOG.warn("R2 image cleanup failed; queued for retry (attempt {}).", deletion.getAttempts());
            }
        }
    }
}
