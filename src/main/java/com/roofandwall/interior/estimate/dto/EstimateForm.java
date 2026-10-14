package com.roofandwall.interior.estimate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class EstimateForm {
    @NotNull(message = "현장을 선택해 주세요.") private Long projectId;
    private long version;
    @NotBlank @Pattern(regexp = "[0-9a-fA-F-]{36}") private String requestKey = UUID.randomUUID().toString();
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;
    @Size(max = 2000) private String scopeOfWork;
    @Size(max = 2000) private String excludedWork;
    @NotNull @Min(0) @Max(999999999) private Long discount = 0L;
    @NotNull @DecimalMin("0") @DecimalMax("1") @Digits(integer = 1, fraction = 4)
    private BigDecimal taxRate = new BigDecimal("0.10");
    @Min(0) @Max(999999999) private Long displayedTotal;
    @NotNull @Size(max = 100, message = "견적 항목은 최대 100개입니다.") @Valid
    private List<@NotNull(message = "견적 항목을 확인해 주세요.") @Valid Item> items = new ArrayList<>();

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long value) { projectId = value; }
    public long getVersion() { return version; }
    public void setVersion(long value) { version = value; }
    public String getRequestKey() { return requestKey; }
    public void setRequestKey(String value) { requestKey = value; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate value) { validUntil = value; }
    public String getScopeOfWork() { return scopeOfWork; }
    public void setScopeOfWork(String value) { scopeOfWork = value; }
    public String getExcludedWork() { return excludedWork; }
    public void setExcludedWork(String value) { excludedWork = value; }
    public Long getDiscount() { return discount; }
    public void setDiscount(Long value) { discount = value; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal value) { taxRate = value; }
    public Long getDisplayedTotal() { return displayedTotal; }
    public void setDisplayedTotal(Long value) { displayedTotal = value; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> value) { items = value; }

    public static class Item {
        @NotBlank @Pattern(regexp = "MATERIAL|LABOR|DEMOLITION|TRANSPORT|OTHER")
        private String category = "MATERIAL";
        @Size(max = 200) private String name = "";
        @Size(max = 20) private String unit = "";
        @DecimalMin("0.01") @DecimalMax("999999.99") @Digits(integer = 6, fraction = 2)
        private BigDecimal quantity;
        @Min(0) @Max(999999999) private Long unitPrice;
        @Size(max = 500) private String freeItemReason;
        public String getCategory() { return category; }
        public void setCategory(String value) { category = value; }
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public String getUnit() { return unit; }
        public void setUnit(String value) { unit = value; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal value) { quantity = value; }
        public Long getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Long value) { unitPrice = value; }
        public String getFreeItemReason() { return freeItemReason; }
        public void setFreeItemReason(String value) { freeItemReason = value; }
    }
}
