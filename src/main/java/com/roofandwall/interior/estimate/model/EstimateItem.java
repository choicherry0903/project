package com.roofandwall.interior.estimate.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "estimate_items")
public class EstimateItem extends BaseEntity {

    @Column(nullable = false)
    private Long estimateId;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 500)
    private String freeItemReason;

    protected EstimateItem() {}

    public EstimateItem(
        Long companyId,
        Long estimateId,
        String category,
        String name,
        String unit,
        BigDecimal quantity,
        Long unitPrice,
        Long amount,
        String freeItemReason
    ) {
        super(companyId);
        this.estimateId = estimateId;
        this.category = category;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.freeItemReason = freeItemReason;
    }

    public Long getEstimateId() {
        return estimateId;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public Long getAmount() {
        return amount;
    }

    public String getFreeItemReason() {
        return freeItemReason;
    }
}
