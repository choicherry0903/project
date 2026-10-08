package com.roofandwall.interior.attachment.repository;

import com.roofandwall.interior.attachment.model.Attachment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByIdAndCompanyId(Long id, Long companyId);
}
