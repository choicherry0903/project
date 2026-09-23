package com.barunsigong.interior.purchase.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** MAT-02: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class PurchaseOrderController {

    @GetMapping("/purchase-orders") public String index(Model model) {

        model.addAttribute("screenId","MAT-02");
        model.addAttribute("title","자재 발주");
        model.addAttribute("description","2차 기능: 발주·부분입고·반품 및 비용 연결");
        return "purchase/list";
    }
}
