package com.roofandwall.interior.project.controller;

import com.roofandwall.interior.project.service.ProjectQueryService;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectListController {

    private final ProjectQueryService projectQueryService;

    public ProjectListController(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/projects";
    }

    @GetMapping("/projects")
    public String list(Model model, @AuthenticationPrincipal StaffPrincipal staff) {
        model.addAttribute("projects", projectQueryService.findAll(staff.getCompanyId()));
        return "project/list";
    }
}
