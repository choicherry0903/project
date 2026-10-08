package com.roofandwall.interior.mobile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** MOB-02: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class MobileTaskDetailController {

    @GetMapping("/mobile/task-detail")
    public String index(Model model) {
        model.addAttribute("screenId", "MOB-02");
        model.addAttribute("title", "작업 상세");
        model.addAttribute("description", "배정 검증 후 작업 시작·완료 요청·일지 저장");
        return "mobile/detail";
    }
}
