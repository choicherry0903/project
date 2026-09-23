package com.barunsigong.interior.purchase.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 200)
    private String supplierName;

    @Column(nullable = false)
    private LocalDate expectedDate;

    @Column(nullable = false, length = 30)
    private String status;

    protected PurchaseOrder() {}

    public PurchaseOrder(Long companyId, Long projectId, String supplierName, LocalDate expectedDate, String status) {
        super(companyId);
        this.projectId = projectId;
        this.supplierName = supplierName;
        this.expectedDate = expectedDate;
        this.status = status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public String getStatus() {
        return status;
    }
}
