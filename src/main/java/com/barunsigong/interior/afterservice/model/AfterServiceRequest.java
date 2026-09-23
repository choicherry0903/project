package com.barunsigong.interior.afterservice.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "after_service_requests")
public class AfterServiceRequest extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column()
    private Long previousRequestId;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(nullable = false, length = 2000)
    private String symptom;

    @Column(nullable = false, length = 20)
    private String urgency;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 2000)
    private String resolution;

    protected AfterServiceRequest() {}

    public AfterServiceRequest(
        Long companyId,
        Long projectId,
        Long previousRequestId,
        LocalDate receivedDate,
        String symptom,
        String urgency,
        String status,
        String resolution
    ) {
        super(companyId);
        this.projectId = projectId;
        this.previousRequestId = previousRequestId;
        this.receivedDate = receivedDate;
        this.symptom = symptom;
        this.urgency = urgency;
        this.status = status;
        this.resolution = resolution;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getPreviousRequestId() {
        return previousRequestId;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public String getSymptom() {
        return symptom;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getStatus() {
        return status;
    }

    public String getResolution() {
        return resolution;
    }
}
