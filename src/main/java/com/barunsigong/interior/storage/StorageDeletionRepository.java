package com.barunsigong.interior.storage;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageDeletionRepository extends JpaRepository<StorageDeletion, String> {
    List<StorageDeletion> findTop20ByAvailableAtLessThanEqualOrderByAvailableAtAsc(Instant now);
}
