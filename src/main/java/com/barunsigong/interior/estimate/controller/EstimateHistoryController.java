package com.barunsigong.interior.estimate.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** EST-03: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class EstimateHistoryController {

    @GetMapping("/estimates/history") public String index(Model model) {

        model.addAttribute("screenId","EST-03");
        model.addAttribute("title","견적 이력");
        model.addAttribute("description","확정 견적 보존, 개정 사유와 버전별 조회");
        return "estimate/history";
    }
}
