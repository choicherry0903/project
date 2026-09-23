package com.barunsigong.interior.material.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** MAT-01: 화면 경로와 뷰 연결. 저장 기능은 docs/screen-map.md의 후속 작업을 참고하세요. */
@Controller
public class MaterialController {

    @GetMapping("/materials") public String index(Model model) {

        model.addAttribute("screenId","MAT-01");
        model.addAttribute("title","자재 기준정보");
        model.addAttribute("description","품명·품번·판매 단가·롤 기준 관리");
        return "material/list";
    }
}
