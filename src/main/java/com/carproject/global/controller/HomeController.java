package com.carproject.global.controller;

import com.carproject.landing.repository.LandingBannerRepository;
import com.carproject.main.service.MainService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LandingBannerRepository bannerRepository;
    private final MainService mainService;

    public HomeController(LandingBannerRepository bannerRepository, MainService mainService) {
        this.bannerRepository = bannerRepository;
        this.mainService = mainService;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("banners", bannerRepository.findByIsVisibleOrderBySortOrderAsc("Y"));
        return "landing";
    }

    // ✅ 여기서 메인에 DB 데이터 내려줌 (BEST CAR TOP6)
    @GetMapping("/main")
    public String main(Model model) {
        model.addAttribute("bestCars", mainService.getBestTop6(PageRequest.of(0, 6)));
        model.addAttribute("brandPopular", mainService.getBrandPopular(6, 4));
        return "main"; // 템플릿: src/main/resources/templates/main.html
    }

    // UX-friendly short URLs (no conflicts with existing controllers)
    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/auth/login";
    }

    @GetMapping("/signup")
    public String signupRedirect() {
        return "redirect:/auth/signup";
    }

    @GetMapping("/events")
    public String eventsRedirect() {
        return "redirect:/event";
    }

    @GetMapping({"/index.html", "/index"})
    public String indexRedirect() {
        return "redirect:/main";
    }
}
