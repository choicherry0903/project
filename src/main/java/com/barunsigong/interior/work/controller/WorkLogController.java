package com.barunsigong.interior.work.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** WRK-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class WorkLogController {

    @GetMapping("/work-logs")
    public String index(Model model) {
        model.addAttribute("screenId", "WRK-01");
        model.addAttribute("title", "작업 기록");
        model.addAttribute("description", "작업 일지·이슈 입력과 작성자별 수정 권한");
        return "work/list";
    }
}
