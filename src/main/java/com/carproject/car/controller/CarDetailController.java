package com.carproject.car.controller;

import com.carproject.car.service.CarDetailService;
import com.carproject.car.repository.CarImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.carproject.car.entity.CarImage;


@Controller
@RequiredArgsConstructor
public class CarDetailController {

    private final CarDetailService carDetailService;
    private final CarImageRepository carImageRepository;
    // 차량 옵션을 고르는 페이지로 넘어가는 컨트롤러 -> 즉 상세페이지
    @GetMapping("/cars/{modelId}")
    public String carDetail(@PathVariable Long modelId, Model model) {
        model.addAttribute("model", carDetailService.getCarDetail(modelId));

        // ✅ 대표 이미지 1장 (현재 정책: COLOR_ID=1 고정, VIEW_TYPE='exterior' 우선)
        String mainImageUrl = carImageRepository
                .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(modelId, "exterior")
                .or(() -> carImageRepository.findFirstByModel_ModelIdOrderByImageIdAsc(modelId))
                .map(CarImage::getImageUrl)
                .orElse("/images/logo_1.png");

        model.addAttribute("mainImageUrl", mainImageUrl);
        return "car/search/cars-detail";
    }

}
