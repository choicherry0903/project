package com.roofandwall.interior.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** SCH-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class ScheduleController {

    @GetMapping("/schedules")
    public String index(Model model) {
        model.addAttribute("screenId", "SCH-01");
        model.addAttribute("title", "일정 캘린더");
        model.addAttribute("description", "월·주·목록 조회와 일정 유형별 필터");
        return "schedule/calendar";
    }
}
