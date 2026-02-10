package com.carproject.unique.dto;

import java.util.List;

public record PyramidViewDto(
        String query,

        Long selectedTrimId,
        Long selectedModelId,
        String selectedName,

        // ✅ 추가: 내 차량 exterior 이미지(없으면 null)
        String selectedImageUrl,

        Long selectedBasePrice,
        Integer selectedTier,

        List<TrimSearchItemDto> results,

        Integer viewTier,
        TierCarCardDto tierMain,
        List<TierCarCardDto> tierCars
) {}
