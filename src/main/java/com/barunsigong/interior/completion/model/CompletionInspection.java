package com.barunsigong.interior.completion.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "completion_inspections")
public class CompletionInspection extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long requesterId;

    @Column()
    private Long approverId;

    @Column(nullable = false)
    private boolean scopeChecked;

    @Column(nullable = false)
    private boolean finishChecked;

    @Column(nullable = false)
    private boolean cleanupChecked;

    @Column()
    private Instant approvedAt;

    @Column(length = 1000)
    private String feedback;

    protected CompletionInspection() {}

    public CompletionInspection(
        Long companyId,
        Long projectId,
        Long requesterId,
        Long approverId,
        boolean scopeChecked,
        boolean finishChecked,
        boolean cleanupChecked,
        Instant approvedAt,
        String feedback
    ) {
        super(companyId);
        this.projectId = projectId;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.scopeChecked = scopeChecked;
        this.finishChecked = finishChecked;
        this.cleanupChecked = cleanupChecked;
        this.approvedAt = approvedAt;
        this.feedback = feedback;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getApproverId() {
        return approverId;
    }

    public boolean isScopeChecked() {
        return scopeChecked;
    }

    public boolean isFinishChecked() {
        return finishChecked;
    }

    public boolean isCleanupChecked() {
        return cleanupChecked;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public String getFeedback() {
        return feedback;
    }
}
