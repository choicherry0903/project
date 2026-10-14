package com.barunsigong.interior.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 고객·현장 목록의 화면 경로만 제공한다. 조회·검색·등록은 이후 구현한다. */
@Controller
public class ProjectListController {

    @GetMapping("/")
    public String home() {
        return "redirect:/projects";
    }

    @GetMapping("/projects")
    public String list() {
        return "project/list";
    }
}
