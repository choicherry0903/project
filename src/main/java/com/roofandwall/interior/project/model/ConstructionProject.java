package com.roofandwall.interior.project.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 업무 데이터 */
@Entity
@Table(name = "construction_projects")
public class ConstructionProject extends BaseEntity {

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 200)
    private String detailAddress;

    @Column(nullable = false, length = 50)
    private String constructionType;

    @Column()
    private LocalDate desiredStartDate;

    @Column(length = 50)
    private String housingType;

    @Column(nullable = false)
    private boolean occupied;

    @Column(length = 2000)
    private String siteNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status;

    @Column(nullable = false)
    private boolean onHold;

    @Column(length = 1000)
    private String holdReason;

    @Column(nullable = false, unique = true, length = 36)
    private String requestKey;

    protected ConstructionProject() {}

    public ConstructionProject(
        Long companyId,
        Long customerId,
        String name,
        String address,
        String detailAddress,
        String constructionType,
        LocalDate desiredStartDate,
        String housingType,
        boolean occupied,
        String siteNotes,
        ProjectStatus status,
        boolean onHold,
        String holdReason,
        String requestKey
    ) {
        super(companyId);
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.detailAddress = detailAddress;
        this.constructionType = constructionType;
        this.desiredStartDate = desiredStartDate;
        this.housingType = housingType;
        this.occupied = occupied;
        this.siteNotes = siteNotes;
        this.status = status;
        this.onHold = onHold;
        this.holdReason = holdReason;
        this.requestKey = requestKey;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public String getConstructionType() {
        return constructionType;
    }

    public LocalDate getDesiredStartDate() {
        return desiredStartDate;
    }

    public String getHousingType() {
        return housingType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getSiteNotes() {
        return siteNotes;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public boolean isOnHold() {
        return onHold;
    }

    public String getHoldReason() {
        return holdReason;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void updateDetails(
        Long customerId,
        String name,
        String address,
        String detailAddress,
        String constructionType,
        LocalDate desiredStartDate,
        String housingType,
        boolean occupied,
        String siteNotes
    ) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.detailAddress = detailAddress;
        this.constructionType = constructionType;
        this.desiredStartDate = desiredStartDate;
        this.housingType = housingType;
        this.occupied = occupied;
        this.siteNotes = siteNotes;
    }

    public String getStatusLabel() {
        return switch (status) {
            case CONSULTATION -> "상담접수";
            case SURVEY_SCHEDULED -> "실측예정";
            case ESTIMATING -> "견적중";
            case CONTRACTED -> "계약완료";
            case IN_PROGRESS -> "시공중";
            case COMPLETED -> "완료";
            case CANCELLED -> "취소";
        };
    }
}
