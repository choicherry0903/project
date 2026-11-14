package com.roofandwall.interior.customer.dto;

import java.util.ArrayList;
import java.util.List;

/** 고객 목록 조회와 등록 입력에 공통으로 사용하는 데이터 객체. */
public class CustomerItem {

    private long id;
    private long customerId;
    private Long customerVersion;
    private Long projectVersion;
    private String requestKey;
    private String customerName;
    private String customerPhone;
    private String projectName;
    private String siteAddress;
    private String status;
    private List<ConsultationItem> consultationItems = new ArrayList<>();

    /** Spring이 등록 폼의 입력값을 바인딩할 때 사용한다. */
    public CustomerItem() {
    }

    /** 목록 조회 결과를 담을 때 사용한다. */
    public CustomerItem(long id, String projectName, String customerName,
                        String customerPhone, String siteAddress, String status) {
        this.id = id;
        this.projectName = projectName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.siteAddress = siteAddress;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public Long getCustomerVersion() { return customerVersion; }
    public void setCustomerVersion(Long customerVersion) { this.customerVersion = customerVersion; }
    public Long getProjectVersion() { return projectVersion; }
    public void setProjectVersion(Long projectVersion) { this.projectVersion = projectVersion; }

    public String getRequestKey() { return requestKey; }

    public long getCustomerId() { return customerId; }

    public void setCustomerId(long customerId) { this.customerId = customerId; }

    public void setRequestKey(String requestKey) { this.requestKey = requestKey; }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSiteAddress() {
        return siteAddress;
    }

    public void setSiteAddress(String siteAddress) {
        this.siteAddress = siteAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String statusLabel() {
        return switch (status == null ? "" : status) {
            case "CONSULTATION" -> "상담";
            case "SURVEY_SCHEDULED" -> "현장 확인 예정";
            case "ESTIMATING" -> "견적";
            case "CONTRACTED" -> "계약";
            case "IN_PROGRESS" -> "시공";
            case "COMPLETED" -> "완료·AS";
            case "CANCELLED" -> "취소";
            default -> status == null || status.isBlank() ? "미지정" : status;
        };
    }

    public List<ConsultationItem> getConsultationItems() {
        return consultationItems;
    }

    public void setConsultationItems(List<ConsultationItem> consultationItems) {
        this.consultationItems = consultationItems;
    }

    /** consultationItems[0].category 형식의 각 상담 행을 받는다. */
    public static class ConsultationItem {

        private String category;
        private String customCategory;
        private String content;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCustomCategory() {
            return customCategory;
        }

        public void setCustomCategory(String customCategory) {
            this.customCategory = customCategory;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
