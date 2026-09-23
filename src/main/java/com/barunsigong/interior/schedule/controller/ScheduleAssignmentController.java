package com.barunsigong.interior.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** SCH-02: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class ScheduleAssignmentController {

    @GetMapping("/schedules/new")
    public String index(Model model) {
        model.addAttribute("screenId", "SCH-02");
        model.addAttribute("title", "일정과 작업자 배정");
        model.addAttribute("description", "작업자별 시간 충돌 검증과 배정 알림");
        return "schedule/form";
    }
}
