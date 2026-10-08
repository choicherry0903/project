package com.roofandwall.interior.contract.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** CON-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class ContractController {

    @GetMapping("/contracts")
    public String index(Model model) {
        model.addAttribute("screenId", "CON-01");
        model.addAttribute("title", "계약 기록");
        model.addAttribute("description", "확정 견적 연결, 동의 기록, 계약금·중도금·잔금 계획");
        return "contract/form";
    }
}
