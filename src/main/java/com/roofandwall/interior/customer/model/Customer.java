package com.roofandwall.interior.customer.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 업무 데이터 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 100)
    private String source;

    @Column(length = 2000)
    private String memo;

    @Column(nullable = false, unique = true, length = 36)
    private String requestKey;

    @Column()
    private Long managerId;

    @Column(nullable = false)
    private boolean archived;

    protected Customer() {}

    public Customer(
        Long companyId,
        String name,
        String phone,
        String source,
        String memo,
        String requestKey,
        Long managerId,
        boolean archived
    ) {
        super(companyId);
        this.name = name;
        this.phone = phone;
        this.source = source;
        this.memo = memo;
        this.requestKey = requestKey;
        this.managerId = managerId;
        this.archived = archived;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getSource() {
        return source;
    }

    public String getMemo() {
        return memo;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public Long getManagerId() {
        return managerId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void updateDetails(String name, String phone, String source, String memo) {
        this.name = name;
        this.phone = phone;
        this.source = source;
        this.memo = memo;
    }

    public String getMaskedPhone() {
        return phone.length() < 8 ? "****" : phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }
}
