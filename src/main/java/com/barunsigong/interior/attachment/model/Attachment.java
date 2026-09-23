package com.barunsigong.interior.attachment.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long uploaderId;

    @Column(nullable = false, length = 30)
    private String recordType;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false, unique = true, length = 200)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    protected Attachment() {}

    public Attachment(
        Long companyId,
        Long projectId,
        Long uploaderId,
        String recordType,
        Long recordId,
        String storageKey,
        String originalFilename,
        String contentType,
        Long sizeBytes
    ) {
        super(companyId);
        this.projectId = projectId;
        this.uploaderId = uploaderId;
        this.recordType = recordType;
        this.recordId = recordId;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public String getRecordType() {
        return recordType;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }
}
