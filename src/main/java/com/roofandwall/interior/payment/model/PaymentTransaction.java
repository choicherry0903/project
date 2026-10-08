package com.roofandwall.interior.payment.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseEntity {

    @Column(nullable = false)
    private Long contractId;

    @Column(nullable = false)
    private Long planId;

    @Column()
    private Long originalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 30)
    private String method;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false, unique = true, length = 36)
    private String requestKey;

    protected PaymentTransaction() {}

    public PaymentTransaction(
        Long companyId,
        Long contractId,
        Long planId,
        Long originalTransactionId,
        TransactionType type,
        LocalDate transactionDate,
        Long amount,
        String method,
        String reason,
        String requestKey
    ) {
        super(companyId);
        this.contractId = contractId;
        this.planId = planId;
        this.originalTransactionId = originalTransactionId;
        this.type = type;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.method = method;
        this.reason = reason;
        this.requestKey = requestKey;
    }

    public Long getContractId() {
        return contractId;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public Long getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getReason() {
        return reason;
    }

    public String getRequestKey() {
        return requestKey;
    }
}
