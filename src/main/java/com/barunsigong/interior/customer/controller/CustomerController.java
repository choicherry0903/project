package com.barunsigong.interior.customer.controller;

import com.barunsigong.interior.customer.dto.CustomerForm;
import com.barunsigong.interior.customer.service.CustomerService;
import com.barunsigong.interior.project.service.ConstructionProjectService;
import jakarta.validation.Valid;
import org.springframework.dao.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customers;
    private final ConstructionProjectService projects;

    public CustomerController(CustomerService customers, ConstructionProjectService projects) {
        this.customers = customers;
        this.projects = projects;
    }

    @GetMapping
    public String list(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        model.addAttribute("customers", customers.search(keyword, page));
        model.addAttribute("keyword", keyword);
        return "customer/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new CustomerForm());
        return form(model, null);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customers.get(id));
        model.addAttribute("projects", projects.forCustomer(id));
        return "customer/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var customer = customers.get(id);
        var form = new CustomerForm();
        form.setName(customer.getName());
        form.setPhone(customer.getPhone());
        form.setSource(customer.getSource());
        form.setMemo(customer.getMemo());
        form.setVersion(customer.getVersion());
        form.setRequestKey(customer.getRequestKey());
        model.addAttribute("form", form);
        return form(model, id);
    }

    @PostMapping({ "", "/{id}" })
    public String save(
        @PathVariable(required = false) Long id,
        @Valid @ModelAttribute("form") CustomerForm form,
        BindingResult errors,
        Model model,
        RedirectAttributes redirect
    ) {
        if (id != null) customers.get(id);
        if (errors.hasErrors()) return form(model, id);
        try {
            var customer = customers.save(id, form);
            redirect.addFlashAttribute("success", "고객 정보를 저장했습니다.");
            return "redirect:/customers/" + customer.getId();
        } catch (IllegalArgumentException e) {
            errors.reject("save", e.getMessage());
        } catch (OptimisticLockingFailureException e) {
            errors.reject("conflict", "다른 사용자가 변경했습니다. 입력 내용을 복사한 뒤 새로고침해 주세요.");
        } catch (DataIntegrityViolationException e) {
            errors.reject("duplicate", "저장 요청을 확인하지 못했습니다. 고객 목록에서 저장 결과를 확인해 주세요.");
        }
        return form(model, id);
    }

    private String form(Model model, Long id) {
        model.addAttribute("customerId", id);
        return "customer/form";
    }
}
