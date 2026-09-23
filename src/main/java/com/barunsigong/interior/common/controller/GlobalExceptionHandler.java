package com.barunsigong.interior.common.controller;

import com.barunsigong.interior.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound(HttpServletResponse response, Model model) {
        response.setStatus(404);
        model.addAttribute("message", "요청한 정보를 찾을 수 없습니다.");
        return "error/404";
    }
}
