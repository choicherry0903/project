package com.barunsigong.interior.purchase.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity @Table(name = "purchase_order_items")
public class PurchaseOrderItem extends BaseEntity {
    @Column(nullable = false) private Long purchaseOrderId;
    @Column(nullable = false) private Long materialId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal quantity;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal receivedQuantity;
    @Column(nullable = false) private Long unitCost;
    protected PurchaseOrderItem() {}
    public PurchaseOrderItem(Long companyId, Long purchaseOrderId, Long materialId, BigDecimal quantity, BigDecimal receivedQuantity, Long unitCost) {
        super(companyId);
        this.purchaseOrderId = purchaseOrderId;
        this.materialId = materialId;
        this.quantity = quantity;
        this.receivedQuantity = receivedQuantity;
        this.unitCost = unitCost;
    }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public Long getMaterialId() { return materialId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public Long getUnitCost() { return unitCost; }
}
