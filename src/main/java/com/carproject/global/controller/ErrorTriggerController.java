package com.carproject.global.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/error-trigger")
public class ErrorTriggerController {

    // 403 템플릿(templates/error/403.html)로 보내기
    @GetMapping("/403")
    public void trigger403() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "의도적으로 403 트리거");
    }

    // 404 템플릿(templates/error/404.html)로 보내기
    @GetMapping("/404")
    public void trigger404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "의도적으로 404 트리거");
    }

    // 500 템플릿(templates/error/500.html)로 보내기
    @GetMapping("/500")
    public void trigger500() {
        throw new RuntimeException("의도적으로 500 트리거");
    }

    // (선택) 400도 필요하면: templates/error/400.html 있으면 그걸로 감
    @GetMapping("/400")
    public void trigger400() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "의도적으로 400 트리거");
    }
}
