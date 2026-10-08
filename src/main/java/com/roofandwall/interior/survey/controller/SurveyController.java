package com.roofandwall.interior.survey.controller;

import com.roofandwall.interior.project.service.ConstructionProjectService;
import com.roofandwall.interior.survey.dto.RoomMeasurementForm;
import com.roofandwall.interior.survey.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projects/{projectId}/survey")
public class SurveyController {

    private final ConstructionProjectService projects;
    private final SurveyService surveys;

    public SurveyController(ConstructionProjectService projects, SurveyService surveys) {
        this.projects = projects;
        this.surveys = surveys;
    }

    @GetMapping
    public String index(@PathVariable Long projectId, Model model) {
        model.addAttribute("project", projects.get(projectId));
        model.addAttribute("rooms", surveys.latest(projectId));
        return "survey/index";
    }

    @GetMapping("/new")
    public String create(@PathVariable Long projectId, Model model) {
        model.addAttribute("form", new RoomMeasurementForm());
        return form(projectId, null, model);
    }

    @GetMapping("/{measurementId}/edit")
    public String edit(@PathVariable Long projectId, @PathVariable Long measurementId, Model model) {
        model.addAttribute("form", RoomMeasurementForm.from(surveys.current(projectId, measurementId)));
        return form(projectId, measurementId, model);
    }

    @GetMapping("/{measurementId}/history")
    public String history(@PathVariable Long projectId, @PathVariable Long measurementId, Model model) {
        var revisions = surveys.history(projectId, measurementId);
        model.addAttribute("project", projects.get(projectId));
        model.addAttribute("revisions", revisions);
        model.addAttribute("roomName", revisions.getFirst().measurement().getRoomName());
        return "survey/history";
    }

    @PostMapping({ "", "/{measurementId}" })
    public String save(
        @PathVariable Long projectId,
        @PathVariable(required = false) Long measurementId,
        @Valid @ModelAttribute("form") RoomMeasurementForm measurementForm,
        BindingResult errors,
        Model model,
        RedirectAttributes redirect
    ) {
        projects.get(projectId);
        if (errors.hasErrors()) return form(projectId, measurementId, model);
        try {
            surveys.save(projectId, measurementId, measurementForm);
            redirect.addFlashAttribute("success", "실측 기록을 저장했습니다.");
            return "redirect:/projects/" + projectId + "/survey";
        } catch (IllegalArgumentException e) {
            errors.reject("save", e.getMessage());
        } catch (DataIntegrityViolationException | PessimisticLockingFailureException e) {
            errors.reject("conflict", "다른 사용자가 실측을 수정했습니다. 최신 기록을 확인해 주세요.");
        }
        return form(projectId, measurementId, model);
    }

    private String form(Long projectId, Long measurementId, Model model) {
        model.addAttribute("project", projects.get(projectId));
        model.addAttribute("measurementId", measurementId);
        return "survey/form";
    }
}
