package com.carproject.car.controller;

import com.carproject.car.dto.ForMeAnswerDto;
import com.carproject.car.service.BrandService;
import com.carproject.car.service.ForMeRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Comparator;


@Controller
@RequiredArgsConstructor
public class CarForMeController {

    private final ForMeRecommendationService forMeRecommendationService;
    private final BrandService brandService; // ✅ 추가

    @GetMapping("/cars/for-me")
    public String forMeForm(Model model) {
        model.addAttribute("answers", new ForMeAnswerDto());
        model.addAttribute("brands", brandService.findAll()); // ✅ 추가

        var brands = brandService.findAll();
        brands.sort(Comparator.comparing(b -> b.getBrandName().toLowerCase())); // ✅ 정렬
        model.addAttribute("brands", brands);

        return "car/for-me/for-me";
    }

    @PostMapping("/cars/for-me/result")
    public String forMeResult(@ModelAttribute("answers") ForMeAnswerDto answers, Model model) {

        System.out.println("originPreference = " + answers.getOriginPreference());
        System.out.println("passengers = " + answers.getPassengers());
        System.out.println("budget = " + answers.getBudget());
        System.out.println("powertrains = " + answers.getPowertrains());


        model.addAttribute("recommends", forMeRecommendationService.recommend(answers, 3));
        return "car/for-me/result";
    }


}
