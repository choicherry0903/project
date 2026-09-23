package com.barunsigong.interior.schedule.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity @Table(name = "worker_assignments")
public class WorkerAssignment extends BaseEntity {
    @Column(nullable = false) private Long scheduleId;
    @Column(nullable = false) private Long workerId;
    @Column(nullable = false) private boolean teamLeader;
    protected WorkerAssignment() {}
    public WorkerAssignment(Long companyId, Long scheduleId, Long workerId, boolean teamLeader) {
        super(companyId);
        this.scheduleId = scheduleId;
        this.workerId = workerId;
        this.teamLeader = teamLeader;
    }
    public Long getScheduleId() { return scheduleId; }
    public Long getWorkerId() { return workerId; }
    public boolean isTeamLeader() { return teamLeader; }
}
