package com.carproject.unique.controller;

import com.carproject.unique.dto.UniqueComparePageDto;
import com.carproject.unique.dto.UniqueUpsellCandidatesPageDto;
import com.carproject.unique.dto.UniqueUpsellPickPageDto;
import com.carproject.unique.service.UniqueUpsellService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/unique/upsell")
public class UniqueUpsellController {

    private final UniqueUpsellService uniqueUpsellService;

    /**
     * Step1: pick page
     * - brands: 체크박스 여러개
     * - segments: 체크박스 여러개
     * - q: 검색어
     * - addPrice: +500만원 등
     * - page: 페이징
     * - modelId: (있으면) 특정 모델 대표 1개 노출
     */
    @GetMapping
    public String pickPage(
            @RequestParam(value = "brands", required = false) List<String> brands,
            @RequestParam(value = "segments", required = false) List<String> segments,
            @RequestParam(value = "modelId", required = false) Long modelId,
            @RequestParam(value = "addPrice", required = false) Long addPrice,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "q", required = false) String keyword,
            Model model
    ) {
        UniqueUpsellPickPageDto dto =
                uniqueUpsellService.buildPickPage(brands, segments, modelId, addPrice, page, keyword);

        model.addAttribute("page", dto);
        return "unique/upsell/upsell";
    }

    /**
     * Step2: candidates page
     * - trimId: 선택한 대표 트림
     * - addPrice: 예산 범위
     */
    @GetMapping("/{trimId}")
    public String candidatesPage(
            @PathVariable("trimId") Long trimId,
            @RequestParam(value = "addPrice", required = false) Long addPrice,
            Model model
    ) {
        UniqueUpsellCandidatesPageDto dto =
                uniqueUpsellService.buildCandidatesPage(trimId, addPrice);

        model.addAttribute("page", dto);
        return "unique/upsell/candidates";
    }

    /**
     * Step3: compare page (있으면 유지)
     */
    @GetMapping("/compare")
    public String comparePage(
            @RequestParam("leftTrimId") Long leftTrimId,
            @RequestParam("rightTrimId") Long rightTrimId,
            Model model
    ) {
        UniqueComparePageDto dto = uniqueUpsellService.buildComparePage(leftTrimId, rightTrimId);
        model.addAttribute("page", dto);
        return "unique/upsell/compare";
    }
}