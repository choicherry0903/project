package com.roofandwall.interior.audit.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "audit_events")
public class AuditEvent extends BaseEntity {

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false, length = 30)
    private String recordType;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 4000)
    private String beforeValue;

    @Column(length = 4000)
    private String afterValue;

    @Column(length = 1000)
    private String reason;

    protected AuditEvent() {}

    public AuditEvent(
        Long companyId,
        Long actorId,
        String recordType,
        Long recordId,
        String action,
        String beforeValue,
        String afterValue,
        String reason
    ) {
        super(companyId);
        this.actorId = actorId;
        this.recordType = recordType;
        this.recordId = recordId;
        this.action = action;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.reason = reason;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getRecordType() {
        return recordType;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getAction() {
        return action;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public String getReason() {
        return reason;
    }
}
