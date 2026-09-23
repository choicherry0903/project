package com.barunsigong.interior.afterservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** AS-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class AfterServiceController {

    @GetMapping("/after-service")
    public String index(Model model) {
        model.addAttribute("screenId", "AS-01");
        model.addAttribute("title", "A/S 접수");
        model.addAttribute("description", "2차 기능: 완료 현장의 하자 접수와 처리 이력");
        return "afterservice/list";
    }
}
