package com.barunsigong.interior.customer.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 업무 데이터 */
@Entity
@Table(name = "consultations")
public class Consultation extends BaseEntity {

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 2000)
    private String content;

    protected Consultation() {}

    public Consultation(Long companyId, Long customerId, Long authorId, String content) {
        super(companyId);
        this.customerId = customerId;
        this.authorId = authorId;
        this.content = content;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }
}
