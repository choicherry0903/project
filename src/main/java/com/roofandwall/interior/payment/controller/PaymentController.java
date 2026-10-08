package com.roofandwall.interior.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** PAY-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class PaymentController {

    @GetMapping("/payments")
    public String index(Model model) {
        model.addAttribute("screenId", "PAY-01");
        model.addAttribute("title", "입금 계획과 수금");
        model.addAttribute("description", "분할 입금·환불·취소 원장과 미수금 집계");
        return "payment/list";
    }
}
