package com.roofandwall.interior.contract.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "construction_contracts")
public class ConstructionContract extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long projectId;

    @Column(nullable = false)
    private Long estimateId;

    @Column(nullable = false)
    private Instant consentedAt;

    @Column(nullable = false, length = 200)
    private String consentMethod;

    @Column(nullable = false, length = 2000)
    private String scopeSnapshot;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(length = 2000)
    private String specialTerms;

    protected ConstructionContract() {}

    public ConstructionContract(
        Long companyId,
        Long projectId,
        Long estimateId,
        Instant consentedAt,
        String consentMethod,
        String scopeSnapshot,
        Long totalAmount,
        String specialTerms
    ) {
        super(companyId);
        this.projectId = projectId;
        this.estimateId = estimateId;
        this.consentedAt = consentedAt;
        this.consentMethod = consentMethod;
        this.scopeSnapshot = scopeSnapshot;
        this.totalAmount = totalAmount;
        this.specialTerms = specialTerms;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getEstimateId() {
        return estimateId;
    }

    public Instant getConsentedAt() {
        return consentedAt;
    }

    public String getConsentMethod() {
        return consentMethod;
    }

    public String getScopeSnapshot() {
        return scopeSnapshot;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public String getSpecialTerms() {
        return specialTerms;
    }
}
