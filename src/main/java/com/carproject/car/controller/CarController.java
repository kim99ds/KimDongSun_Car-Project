package com.carproject.car.controller;

import com.carproject.car.entity.CarImage;
import com.carproject.car.service.BrandService;
import com.carproject.car.service.CarService;
import com.carproject.car.repository.CarImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final BrandService brandService;
    private final CarImageRepository carImageRepository;

    // 차량 전체 목록으로 들어가는 컨트롤러
    @GetMapping("/cars")
    public String cars(
            @RequestParam(required = false) String q,
            // ✅ 원문 검색결과 보기(자동교정 OFF)
            @RequestParam(required = false, defaultValue = "false") boolean nosuggest,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String engineType,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        // ✅ 검색 + 자동교정(0건일 때만)
        var r = carService.searchCarsWithSuggestion(q, nosuggest, segment, brand, engineType, sort);
        model.addAttribute("carModels", r.carModels());
        model.addAttribute("didYouMeanApplied", r.didYouMeanApplied());
        model.addAttribute("originalQ", r.originalQ());
        model.addAttribute("suggestedQ", r.suggestedQ());

        model.addAttribute("brands", brandService.findAll());

        // ✅ cars(리스트) 대표 이미지 정책
        // - VIEW_TYPE='con' 우선, 없으면 VIEW_TYPE='exterior'로 fallback
        // - 최종 fallback은 프론트 기본 이미지
        Map<Long, String> listImageUrlMap = new HashMap<>();
        r.carModels().forEach(cm -> {
            Long modelId = cm.getModelId();
            String url = carImageRepository
                    .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(modelId, "con")
                    .or(() -> carImageRepository.findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(modelId, "exterior"))
                    .or(() -> carImageRepository.findFirstByModel_ModelIdOrderByImageIdAsc(modelId))
                    .map(CarImage::getImageUrl)
                    .orElse(null);

            if (url != null) listImageUrlMap.put(modelId, url);
        });
        model.addAttribute("listImageUrlMap", listImageUrlMap);

        return "car/search/cars";

    }
}
