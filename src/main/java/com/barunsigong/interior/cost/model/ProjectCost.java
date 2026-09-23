package com.barunsigong.interior.cost.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "project_costs")
public class ProjectCost extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private LocalDate incurredDate;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false)
    private Long supplyAmount;

    @Column(nullable = false, length = 30)
    private String paymentStatus;

    @Column(unique = true, length = 100)
    private String sourceKey;

    @Column(length = 1000)
    private String memo;

    protected ProjectCost() {}

    public ProjectCost(
        Long companyId,
        Long projectId,
        LocalDate incurredDate,
        String category,
        Long supplyAmount,
        String paymentStatus,
        String sourceKey,
        String memo
    ) {
        super(companyId);
        this.projectId = projectId;
        this.incurredDate = incurredDate;
        this.category = category;
        this.supplyAmount = supplyAmount;
        this.paymentStatus = paymentStatus;
        this.sourceKey = sourceKey;
        this.memo = memo;
    }

    public Long getProjectId() {
        return projectId;
    }

    public LocalDate getIncurredDate() {
        return incurredDate;
    }

    public String getCategory() {
        return category;
    }

    public Long getSupplyAmount() {
        return supplyAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public String getMemo() {
        return memo;
    }
}
