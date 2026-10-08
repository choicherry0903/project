package com.roofandwall.interior.cost.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** FIN-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class ProjectCostController {

    @GetMapping("/costs")
    public String index(Model model) {
        model.addAttribute("screenId", "FIN-01");
        model.addAttribute("title", "비용과 예상 이익");
        model.addAttribute("description", "2차 기능: 비용 원장·증빙·정정 이력");
        return "cost/list";
    }
}
