package com.roofandwall.interior.work.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "work_logs")
public class WorkLog extends BaseEntity {

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(length = 1000)
    private String issue;

    @Column(nullable = false)
    private boolean resolved;

    protected WorkLog() {}

    public WorkLog(
        Long companyId,
        Long scheduleId,
        Long authorId,
        LocalDate workDate,
        String content,
        String issue,
        boolean resolved
    ) {
        super(companyId);
        this.scheduleId = scheduleId;
        this.authorId = authorId;
        this.workDate = workDate;
        this.content = content;
        this.issue = issue;
        this.resolved = resolved;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public String getContent() {
        return content;
    }

    public String getIssue() {
        return issue;
    }

    public boolean isResolved() {
        return resolved;
    }
}
