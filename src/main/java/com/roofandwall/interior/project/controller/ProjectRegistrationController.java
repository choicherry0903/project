package com.roofandwall.interior.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 등록 화면만 제공한다. POST 등록·수정·삭제 처리는 구현하지 않는다. */
@Controller
public class ProjectRegistrationController {

    @GetMapping("/projects/new")
    public String form() {
        return "project/form";
    }
}
