package com.roofandwall.interior.customer.service;

import com.roofandwall.interior.customer.dto.CustomerItem;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;
import java.util.UUID;

@Component
public class CustomerRegistrationValidator implements Validator {
    private static final Set<String> CATEGORIES = Set.of("샷시", "철거", "확장",
            "설비 (난방, 상수도, 하수도, A/C)", "목공", "욕실", "타일", "전기",
            "필름, 현관철물", "도배", "바닥재", "가구", "탄성코트", "기타");

    @Override
    public boolean supports(Class<?> type) { return CustomerItem.class.isAssignableFrom(type); }

    @Override
    public void validate(Object target, Errors errors) {
        validateDetails((CustomerItem) target, errors);
        var form = (CustomerItem) target;
        try {
            if (!UUID.fromString(form.getRequestKey()).toString().equals(form.getRequestKey())) throw new IllegalArgumentException();
        } catch (IllegalArgumentException | NullPointerException ex) {
            errors.rejectValue("requestKey", "invalid", "등록 요청이 올바르지 않습니다. 다시 등록해 주세요.");
        }
    }

    public void validateDetails(CustomerItem form, Errors errors) {
        check(errors, "customerName", form.getCustomerName(), 50, "고객명");
        check(errors, "customerPhone", form.getCustomerPhone(), 15, "연락처");
        check(errors, "projectName", form.getProjectName(), 100, "현장명");
        check(errors, "siteAddress", form.getSiteAddress(), 200, "현장 주소");
        if (form.getConsultationItems() == null || form.getConsultationItems().size() > 100) {
            errors.reject("rows", "상담 내용은 최대 100행까지 입력할 수 있습니다.");
            return;
        }
        for (int i = 0; i < form.getConsultationItems().size(); i++) {
            var item = form.getConsultationItems().get(i);
            if (item == null) { errors.reject("row", "상담 행을 다시 확인해 주세요."); continue; }
            String field = "consultationItems[" + i + "].";
            boolean hasContent = item.getContent() != null && !item.getContent().isBlank();
            boolean hasCustom = item.getCustomCategory() != null && !item.getCustomCategory().isBlank();
            if (!hasContent && !hasCustom) continue; // 입력하지 않은 기본 행은 저장하지 않는다.
            if (!CATEGORIES.contains(item.getCategory() == null ? "" : item.getCategory()))
                errors.rejectValue(field + "category", "invalid", "상담 항목을 선택해 주세요.");
            if ("기타".equals(item.getCategory())) check(errors, field + "customCategory", item.getCustomCategory(), 100, "기타 항목명");
            check(errors, field + "content", item.getContent(), 2000, "상담 내용");
        }
    }

    private void check(Errors errors, String field, String value, int max, String label) {
        if (value == null || value.isBlank()) errors.rejectValue(field, "required", label + "을(를) 입력해 주세요.");
        else if (value.length() > max) errors.rejectValue(field, "length", label + "은(는) " + max + "자 이내로 입력해 주세요.");
    }

}
