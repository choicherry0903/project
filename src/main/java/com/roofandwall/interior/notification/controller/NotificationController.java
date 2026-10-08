package com.roofandwall.interior.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** NTF-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class NotificationController {

    @GetMapping("/notifications")
    public String index(Model model) {
        model.addAttribute("screenId", "NTF-01");
        model.addAttribute("title", "알림");
        model.addAttribute("description", "수신자별 알림 조회·읽음 처리·중복 방지");
        return "notification/list";
    }
}
