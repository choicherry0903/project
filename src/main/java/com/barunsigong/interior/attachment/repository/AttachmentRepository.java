package com.barunsigong.interior.attachment.repository;
import com.barunsigong.interior.attachment.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByIdAndCompanyId(Long id, Long companyId);
}
