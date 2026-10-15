package com.roofandwall.interior.aicounsult;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** AI 상담 정리의 화면 경로만 제공한다. 모델 호출·저장·외부 연동은 이후 구현한다. */
@Controller
public class AICounsultController {

    @GetMapping("/consultations/ai")
    public String index() {
        return "ai-counsult/ai-counsult";
    }
}
