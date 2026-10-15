package com.roofandwall.interior.customer.dto;

/** 목록에 필요한 조회 전용 데이터. 등록·수정용 객체와 분리한다. */
public record CustomerListItem(
        long id,
        String projectName,
        String customerName,
        String customerPhone,
        String siteAddress,
        String status
) {
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
}
