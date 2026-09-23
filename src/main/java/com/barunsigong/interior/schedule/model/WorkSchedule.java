package com.barunsigong.interior.schedule.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity @Table(name = "work_schedules")
public class WorkSchedule extends BaseEntity {
    @Column(nullable = false) private Long projectId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ScheduleType type;
    @Column(nullable = false) private Instant startsAt;
    @Column(nullable = false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private WorkStatus status;
    @Column(length = 1000) private String notes;
    protected WorkSchedule() {}
    public WorkSchedule(Long companyId, Long projectId, ScheduleType type, Instant startsAt, Instant endsAt, WorkStatus status, String notes) {
        super(companyId);
        this.projectId = projectId;
        this.type = type;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.notes = notes;
    }
    public Long getProjectId() { return projectId; }
    public ScheduleType getType() { return type; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public WorkStatus getStatus() { return status; }
    public String getNotes() { return notes; }
}
