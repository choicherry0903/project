package com.barunsigong.interior.notification.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, unique = true, length = 200)
    private String eventKey;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 500)
    private String targetPath;

    @Column()
    private Instant readAt;

    protected Notification() {}

    public Notification(
        Long companyId,
        Long recipientId,
        String eventKey,
        String message,
        String targetPath,
        Instant readAt
    ) {
        super(companyId);
        this.recipientId = recipientId;
        this.eventKey = eventKey;
        this.message = message;
        this.targetPath = targetPath;
        this.readAt = readAt;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getMessage() {
        return message;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
