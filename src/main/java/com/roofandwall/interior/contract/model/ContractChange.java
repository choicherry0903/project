package com.roofandwall.interior.contract.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "contract_changes")
public class ContractChange extends BaseEntity {

    @Column(nullable = false)
    private Long contractId;

    @Column(nullable = false)
    private Long estimateId;

    @Column(nullable = false)
    private Long deltaAmount;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private Instant consentedAt;

    protected ContractChange() {}

    public ContractChange(
        Long companyId,
        Long contractId,
        Long estimateId,
        Long deltaAmount,
        String reason,
        Instant consentedAt
    ) {
        super(companyId);
        this.contractId = contractId;
        this.estimateId = estimateId;
        this.deltaAmount = deltaAmount;
        this.reason = reason;
        this.consentedAt = consentedAt;
    }

    public Long getContractId() {
        return contractId;
    }

    public Long getEstimateId() {
        return estimateId;
    }

    public Long getDeltaAmount() {
        return deltaAmount;
    }

    public String getReason() {
        return reason;
    }

    public Instant getConsentedAt() {
        return consentedAt;
    }
}
