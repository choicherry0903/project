package com.barunsigong.interior.estimate.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** EST-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class EstimateController {

    @GetMapping("/estimates") public String index(Model model) {

        model.addAttribute("screenId","EST-01");
        model.addAttribute("title","견적 작성");
        model.addAttribute("description","항목별 수량·단가 입력, 서버 금액 계산, 초안 저장");
        return "estimate/form";
    }
}
