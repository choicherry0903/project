package com.barunsigong.interior.completion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** QA-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class CompletionController {

    @GetMapping("/completion")
    public String index(Model model) {
        model.addAttribute("screenId", "QA-01");
        model.addAttribute("title", "완료 점검");
        model.addAttribute("description", "필수 점검, 미해결 이슈 검사, 관리자 완료 승인");
        return "completion/form";
    }
}
