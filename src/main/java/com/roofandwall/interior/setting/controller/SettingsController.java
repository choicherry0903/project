package com.roofandwall.interior.setting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** ADM-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String index(Model model) {
        model.addAttribute("screenId", "ADM-01");
        model.addAttribute("title", "직원과 업체 설정");
        model.addAttribute("description", "직원 등록·비활성화, 마지막 관리자 보호, 업체 출력 정보");
        return "setting/index";
    }
}
