package com.roofandwall.interior.scene;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;

/** 고객·현장과 상담 내용을 조회한다. 수정·삭제는 CustomerController에서 처리한다. */
@Controller
public class SceneController {
    private final CustomerQueryService customerQueryService;

    public SceneController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    @GetMapping("/customers/detail")
    public String detail(@RequestParam(required = false) Long projectId,
                         @AuthenticationPrincipal StaffPrincipal staff, Model model) {
        model.addAttribute("selected", projectId != null);
        model.addAttribute("form", projectId == null ? new CustomerItem()
                : customerQueryService.findDetail(staff.getCompanyId(), projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "고객·현장을 찾을 수 없습니다.")));
        return "customer/customer-detail";
    }
}
