package com.roofandwall.interior.mobile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** MOB-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class MobileTaskController {

    @GetMapping("/mobile/tasks")
    public String index(Model model) {
        model.addAttribute("screenId", "MOB-01");
        model.addAttribute("title", "오늘 작업");
        model.addAttribute("description", "본인에게 배정된 일정만 조회");
        return "mobile/tasks";
    }
}
