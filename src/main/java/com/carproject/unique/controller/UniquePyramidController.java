package com.carproject.unique.controller;

import com.carproject.unique.dto.PyramidViewDto;
import com.carproject.unique.service.UniquePyramidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/unique")
public class UniquePyramidController {

    private final UniquePyramidService uniquePyramidService;

    @GetMapping("/pyramid")
    public String pyramid(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long trimId,
            @RequestParam(required = false) Integer tier,
            @RequestParam(required = false) Long focusTrimId,
            Model model
    ) {
        PyramidViewDto page = uniquePyramidService.buildPage(q, trimId, tier, focusTrimId);
        model.addAttribute("page", page);
        return "unique/pyramid";
    }
}