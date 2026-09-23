package com.barunsigong.interior.survey.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.barunsigong.interior.project.service.ConstructionProjectService;
/** SUR-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class SurveyController {
    private final ConstructionProjectService projects;
    public SurveyController(ConstructionProjectService projects) { this.projects=projects; }
    @GetMapping("/projects/{projectId}/survey") public String index(@PathVariable Long projectId, Model model) {
        projects.get(projectId);
        model.addAttribute("screenId","SUR-01");
        model.addAttribute("title","공간별 실측");
        model.addAttribute("description","둘레·높이·개구부·천장 면적 입력 및 실측 버전 저장");
        return "survey/form";
    }
}
