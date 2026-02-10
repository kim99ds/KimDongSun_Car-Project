package com.carproject.landing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LandingViewController {

    @GetMapping("/landing/{pageId}")
    public String view(@PathVariable String pageId) {
        return pageId;
    }
}
