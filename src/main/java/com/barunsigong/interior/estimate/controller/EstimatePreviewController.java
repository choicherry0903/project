package com.barunsigong.interior.estimate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** EST-02: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class EstimatePreviewController {

    @GetMapping("/estimates/preview")
    public String index(Model model) {
        model.addAttribute("screenId", "EST-02");
        model.addAttribute("title", "견적 미리보기");
        model.addAttribute("description", "업체·고객 정보와 견적 버전을 보존한 PDF 출력");
        return "estimate/preview";
    }
}
