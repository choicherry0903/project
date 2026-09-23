package com.barunsigong.interior.payment.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "payment_plans")
public class PaymentPlan extends BaseEntity {

    @Column(nullable = false)
    private Long contractId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private Long amount;

    protected PaymentPlan() {}

    public PaymentPlan(Long companyId, Long contractId, String name, LocalDate dueDate, Long amount) {
        super(companyId);
        this.contractId = contractId;
        this.name = name;
        this.dueDate = dueDate;
        this.amount = amount;
    }

    public Long getContractId() {
        return contractId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getAmount() {
        return amount;
    }
}
