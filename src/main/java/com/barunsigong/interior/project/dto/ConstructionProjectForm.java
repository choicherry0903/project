package com.barunsigong.interior.project.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public class ConstructionProjectForm {

    @NotNull
    private Long customerId;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String address;

    @Size(max = 200)
    private String detailAddress;

    @NotBlank
    @Size(max = 50)
    private String constructionType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desiredStartDate;

    @Size(max = 50)
    private String housingType;

    private boolean occupied;

    @Size(max = 2000)
    private String siteNotes;

    @Min(0)
    private long version;

    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F-]{36}")
    private String requestKey = UUID.randomUUID().toString();

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }

    public LocalDate getDesiredStartDate() {
        return desiredStartDate;
    }

    public void setDesiredStartDate(LocalDate desiredStartDate) {
        this.desiredStartDate = desiredStartDate;
    }

    public String getHousingType() {
        return housingType;
    }

    public void setHousingType(String housingType) {
        this.housingType = housingType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public String getSiteNotes() {
        return siteNotes;
    }

    public void setSiteNotes(String siteNotes) {
        this.siteNotes = siteNotes;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
