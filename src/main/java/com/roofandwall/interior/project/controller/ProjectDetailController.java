package com.roofandwall.interior.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 특정 현장을 조회하지 않는 상세 화면 골격. 데이터 연결 시 식별자 경로를 추가한다. */
@Controller
public class ProjectDetailController {

    @GetMapping("/projects/detail")
    public String detail() {
        return "project/detail";
    }
}
