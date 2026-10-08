package com.roofandwall.interior.estimate.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "estimates")
public class Estimate extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Integer revision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstimateStatus status;

    @Column()
    private LocalDate validUntil;

    @Column(length = 2000)
    private String scopeOfWork;

    @Column(length = 2000)
    private String excludedWork;

    @Column(length = 1000)
    private String revisionReason;

    @Column(nullable = false)
    private Long discount;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(nullable = false)
    private Long supplyAmount;

    @Column(nullable = false)
    private Long taxAmount;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(length = 2000)
    private String companySnapshot;

    @Column()
    private Instant confirmedAt;

    protected Estimate() {}

    public Estimate(
        Long companyId,
        Long projectId,
        Integer revision,
        EstimateStatus status,
        LocalDate validUntil,
        String scopeOfWork,
        String excludedWork,
        String revisionReason,
        Long discount,
        BigDecimal taxRate,
        Long supplyAmount,
        Long taxAmount,
        Long totalAmount,
        String companySnapshot,
        Instant confirmedAt
    ) {
        super(companyId);
        this.projectId = projectId;
        this.revision = revision;
        this.status = status;
        this.validUntil = validUntil;
        this.scopeOfWork = scopeOfWork;
        this.excludedWork = excludedWork;
        this.revisionReason = revisionReason;
        this.discount = discount;
        this.taxRate = taxRate;
        this.supplyAmount = supplyAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.companySnapshot = companySnapshot;
        this.confirmedAt = confirmedAt;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Integer getRevision() {
        return revision;
    }

    public EstimateStatus getStatus() {
        return status;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getScopeOfWork() {
        return scopeOfWork;
    }

    public String getExcludedWork() {
        return excludedWork;
    }

    public String getRevisionReason() {
        return revisionReason;
    }

    public Long getDiscount() {
        return discount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public Long getSupplyAmount() {
        return supplyAmount;
    }

    public Long getTaxAmount() {
        return taxAmount;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public String getCompanySnapshot() {
        return companySnapshot;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }
}
