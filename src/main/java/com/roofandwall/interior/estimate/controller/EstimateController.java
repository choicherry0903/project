package com.roofandwall.interior.estimate.controller;

import com.roofandwall.interior.estimate.dto.EstimateForm;
import com.roofandwall.interior.estimate.model.EstimateStatus;
import com.roofandwall.interior.estimate.service.EstimateService;
import jakarta.validation.Valid;
import org.springframework.dao.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/estimates")
public class EstimateController {
    private final EstimateService estimates;
    public EstimateController(EstimateService estimates) { this.estimates = estimates; }
    @InitBinder("form")
    void bind(WebDataBinder binder) { binder.setAutoGrowCollectionLimit(100); }
    @ModelAttribute("categories")
    Map<String, String> categories() {
        var result = new LinkedHashMap<String, String>();
        result.put("MATERIAL", "자재"); result.put("LABOR", "인건비"); result.put("DEMOLITION", "철거");
        result.put("TRANSPORT", "운반"); result.put("OTHER", "기타"); return result;
    }
    @GetMapping
    public String list(@RequestParam(required = false) Long projectId,
            @RequestParam(required = false) EstimateStatus status,
            @RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("estimates", estimates.search(projectId, status, keyword, page));
        model.addAttribute("projectNames", estimates.projectChoices().stream().collect(
            java.util.stream.Collectors.toMap(p -> p.getId(), p -> p.getName())));
        model.addAttribute("projectId", projectId); model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status); model.addAttribute("statuses", EstimateStatus.values());
        return "estimate/list";
    }
    @GetMapping("/new")
    public String create(@RequestParam(required = false) Long projectId, Model model) {
        var f = new EstimateForm();
        f.getItems().add(new EstimateForm.Item());
        if (projectId != null) { estimates.project(projectId); f.setProjectId(projectId); }
        model.addAttribute("form", f); return form(model, null);
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var e = estimates.get(id);
        model.addAttribute("estimate", e); model.addAttribute("project", estimates.project(e.getProjectId()));
        model.addAttribute("items", estimates.items(id)); return "estimate/detail";
    }
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try { model.addAttribute("form", estimates.form(id)); return form(model, id); }
        catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); return "redirect:/estimates/" + id; }
    }
    @PostMapping({"", "/{id}"})
    public String save(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") EstimateForm f,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (id != null) estimates.get(id);
        if (errors.hasErrors()) return form(model, id);
        try {
            var e = estimates.save(id, f);
            redirect.addFlashAttribute("success", "견적 초안을 저장했습니다.");
            return "redirect:/estimates/" + e.getId();
        } catch (IllegalArgumentException e) { errors.reject("save", e.getMessage()); }
        catch (OptimisticLockingFailureException e) { errors.reject("conflict", "다른 사용자가 수정했습니다. 새로고침 후 다시 확인해 주세요."); }
        catch (DataIntegrityViolationException e) { errors.reject("duplicate", "견적 목록에서 저장 결과를 확인한 뒤 다시 시도해 주세요."); }
        return form(model, id);
    }
    @GetMapping("/{id}/delete")
    public String deleteForm(@PathVariable Long id, Model model) {
        var e = estimates.get(id);
        model.addAttribute("estimate", e); model.addAttribute("project", estimates.project(e.getProjectId()));
        return "estimate/delete";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam long version, RedirectAttributes redirect) {
        try { estimates.delete(id, version); redirect.addFlashAttribute("success", "견적 초안을 삭제했습니다."); return "redirect:/estimates"; }
        catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            redirect.addFlashAttribute("error", e instanceof IllegalArgumentException ? e.getMessage() : "다른 사용자가 변경했습니다. 다시 확인해 주세요.");
            return "redirect:/estimates/" + id;
        }
    }
    private String form(Model model, Long id) {
        model.addAttribute("estimateId", id); model.addAttribute("projects", estimates.projectChoices());
        model.addAttribute("materials", estimates.materialChoices()); return "estimate/form";
    }
}
