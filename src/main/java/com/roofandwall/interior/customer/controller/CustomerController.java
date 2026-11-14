package com.roofandwall.interior.customer.controller;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindingResult;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import com.roofandwall.interior.customer.service.CustomerRegistrationValidator;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.dao.OptimisticLockingFailureException;

@Controller
public class CustomerController {

    private final CustomerQueryService customerQueryService;
    private final CustomerRegistrationValidator validator;

    public CustomerController(CustomerQueryService customerQueryService, CustomerRegistrationValidator validator) {
        this.customerQueryService = customerQueryService;
        this.validator = validator;
    }

    @InitBinder("form")
    public void bindRegistration(WebDataBinder binder) {
        binder.setAllowedFields("requestKey", "customerVersion", "projectVersion", "customerName", "customerPhone", "projectName", "siteAddress",
                "consultationItems[*].category", "consultationItems[*].customCategory", "consultationItems[*].content");
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("emptyConsultation")
    public CustomerItem.ConsultationItem emptyConsultation() {
        return new CustomerItem.ConsultationItem();
    }

    @GetMapping("/customers")
    public String list(Model model, @AuthenticationPrincipal StaffPrincipal staff) {
        model.addAttribute("result", customerQueryService.findAll(staff.getCompanyId()));
        return "customer/customer-list";
    }

    /** 등록 화면만 제공한다. */
    @GetMapping("/customers/regform")
    public String regform(Model model, @AuthenticationPrincipal StaffPrincipal staff) {
        var form = new CustomerItem();
        form.setRequestKey(UUID.randomUUID().toString());
        form.getConsultationItems().add(new CustomerItem.ConsultationItem());
        model.addAttribute("form", form);
        return "customer/customer-registration";
    }

    /** 저장 로직(등록)*/
    @PostMapping("/customers/insertCustomer")
    public String insertCustomer(@AuthenticationPrincipal StaffPrincipal staff,
                                 @ModelAttribute("form") CustomerItem form, BindingResult errors) {
        validator.validate(form, errors);
        if (errors.hasErrors()) {
            if (errors.hasFieldErrors("requestKey")) form.setRequestKey(UUID.randomUUID().toString());
            return "customer/customer-registration";
        }
        try {
            customerQueryService.insertCustomer(staff.getCompanyId(), staff.getAccountId(), form);
        } catch (DuplicateKeyException ex) {
            // INSERT 실패 트랜잭션이 롤백된 뒤, 같은 업체에서 완료된 요청인지 확인한다.
            try {
                if (customerQueryService.registrationExists(staff.getCompanyId(), form.getRequestKey()))
                    return "redirect:/customers";
            } catch (DataAccessException unavailable) {
                // 연결 실패도 입력 화면에서 재시도할 수 있도록 처리한다.
            }
            errors.reject("save", "등록하지 못했습니다. 입력 내용을 확인한 뒤 다시 시도해 주세요.");
            return "customer/customer-registration";
        } catch (DataAccessException ex) {
            errors.reject("save", "등록하지 못했습니다. 잠시 후 다시 시도해 주세요. 입력 내용은 유지됩니다.");
            return "customer/customer-registration";
        }
        return "redirect:/customers";
    }

    @GetMapping("/customers/{projectId}/edit")
    public String edit(@PathVariable long projectId, @AuthenticationPrincipal StaffPrincipal staff, Model model) {
        model.addAttribute("form", customerQueryService.requireDetail(staff.getCompanyId(), projectId));
        model.addAttribute("editing", true);
        model.addAttribute("projectId", projectId);
        return "customer/customer-registration";
    }

    @PostMapping("/customers/{projectId}/edit")
    public String update(@PathVariable long projectId, @AuthenticationPrincipal StaffPrincipal staff,
                         @ModelAttribute("form") CustomerItem form, BindingResult errors, Model model) {
        var current = customerQueryService.requireDetail(staff.getCompanyId(), projectId);
        model.addAttribute("editing", true);
        model.addAttribute("projectId", projectId);
        form.setStatus(current.getStatus());
        validator.validateDetails(form, errors);
        validateVersions(form, errors);
        if (!errors.hasErrors()) {
            try {
                customerQueryService.updateCustomer(staff.getCompanyId(), projectId, staff.getAccountId(), form);
                return "redirect:/customers/detail?projectId=" + projectId;
            } catch (OptimisticLockingFailureException ex) {
                errors.reject("conflict", "다른 화면에서 정보가 변경되었습니다. 최신 정보를 다시 불러온 뒤 수정해 주세요.");
            } catch (DataAccessException ex) {
                errors.reject("save", "수정하지 못했습니다. 잠시 후 다시 시도해 주세요. 입력 내용은 유지됩니다.");
            }
        }
        return "customer/customer-registration";
    }

    @GetMapping("/customers/{projectId}/delete")
    public String deleteForm(@PathVariable long projectId, @AuthenticationPrincipal StaffPrincipal staff, Model model) {
        model.addAttribute("form", customerQueryService.requireDetail(staff.getCompanyId(), projectId));
        model.addAttribute("projectId", projectId);
        return "customer/customer-delete";
    }

    @PostMapping("/customers/{projectId}/delete")
    public String delete(@PathVariable long projectId, @AuthenticationPrincipal StaffPrincipal staff,
                         @ModelAttribute("form") CustomerItem form, BindingResult errors,
                         @RequestParam(defaultValue = "false") boolean confirmed, Model model) {
        var current = customerQueryService.requireDetail(staff.getCompanyId(), projectId);
        model.addAttribute("projectId", projectId);
        form.setCustomerName(current.getCustomerName());
        form.setProjectName(current.getProjectName());
        validateVersions(form, errors);
        if (!confirmed) errors.reject("confirmation", "삭제 안내를 확인하고 동의해 주세요.");
        if (!errors.hasErrors()) {
            try {
                customerQueryService.archiveCustomer(staff.getCompanyId(), projectId, form);
                return "redirect:/customers";
            } catch (OptimisticLockingFailureException ex) {
                errors.reject("conflict", "정보가 변경되었습니다. 삭제 화면을 다시 열어 최신 정보를 확인해 주세요.");
            } catch (DataAccessException ex) {
                errors.reject("delete", "삭제하지 못했습니다. 잠시 후 다시 시도해 주세요.");
            }
        }
        return "customer/customer-delete";
    }

    private void validateVersions(CustomerItem form, BindingResult errors) {
        if (form.getCustomerVersion() == null || form.getCustomerVersion() < 0
                || form.getProjectVersion() == null || form.getProjectVersion() < 0)
            errors.reject("version", "요청이 올바르지 않습니다. 화면을 다시 열어 주세요.");
    }
}
