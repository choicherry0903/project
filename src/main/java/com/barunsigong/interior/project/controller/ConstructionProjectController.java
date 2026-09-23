package com.barunsigong.interior.project.controller;
import com.barunsigong.interior.customer.service.CustomerService;
import com.barunsigong.interior.project.dto.ConstructionProjectForm;
import com.barunsigong.interior.project.model.ProjectStatus;
import com.barunsigong.interior.project.service.ConstructionProjectService;
import jakarta.validation.Valid;
import org.springframework.dao.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/projects")
public class ConstructionProjectController {
    private final ConstructionProjectService projects;
    private final CustomerService customers;
    public ConstructionProjectController(ConstructionProjectService projects,CustomerService customers) { this.projects=projects; this.customers=customers; }
    @GetMapping public String list(@RequestParam(defaultValue="") String keyword,@RequestParam(required=false) ProjectStatus status,
            @RequestParam(defaultValue="0") int page,Model model) {
        model.addAttribute("projects",projects.search(keyword,status,page)); model.addAttribute("keyword",keyword);
        model.addAttribute("selectedStatus",status); model.addAttribute("statuses",ProjectStatus.values()); return "project/list";
    }
    @GetMapping("/new") public String create(@RequestParam(required=false) Long customerId,Model model) {
        var form=new ConstructionProjectForm(); if(customerId!=null) { customers.get(customerId); form.setCustomerId(customerId); }
        model.addAttribute("form",form); return form(model,null);
    }
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model model) {
        var project=projects.get(id); model.addAttribute("project",project); model.addAttribute("customer",customers.get(project.getCustomerId()));
        return "project/detail";
    }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model) {
        var p=projects.get(id); var f=new ConstructionProjectForm();
        f.setCustomerId(p.getCustomerId()); f.setName(p.getName()); f.setAddress(p.getAddress()); f.setDetailAddress(p.getDetailAddress());
        f.setConstructionType(p.getConstructionType()); f.setDesiredStartDate(p.getDesiredStartDate()); f.setHousingType(p.getHousingType());
        f.setOccupied(p.isOccupied()); f.setSiteNotes(p.getSiteNotes()); f.setVersion(p.getVersion()); f.setRequestKey(p.getRequestKey());
        model.addAttribute("form",f); return form(model,id);
    }
    @PostMapping({"", "/{id}"}) public String save(@PathVariable(required=false) Long id,
            @Valid @ModelAttribute("form") ConstructionProjectForm form,BindingResult errors,Model model,RedirectAttributes redirect) {
        if(id!=null) projects.get(id);
        if(errors.hasErrors()) return form(model,id);
        try {
            var project=projects.save(id,form); redirect.addFlashAttribute("success","현장 정보를 저장했습니다.");
            return "redirect:/projects/"+project.getId();
        } catch(IllegalArgumentException e) { errors.reject("save",e.getMessage()); }
        catch(OptimisticLockingFailureException e) { errors.reject("conflict","다른 사용자가 변경했습니다. 입력 내용을 복사한 뒤 새로고침해 주세요."); }
        catch(DataIntegrityViolationException e) { errors.reject("duplicate","저장 요청을 확인하지 못했습니다. 현장 목록에서 저장 결과를 확인해 주세요."); }
        return form(model,id);
    }
    private String form(Model model,Long id) { model.addAttribute("projectId",id); model.addAttribute("customers",customers.choices()); return "project/form"; }
}
