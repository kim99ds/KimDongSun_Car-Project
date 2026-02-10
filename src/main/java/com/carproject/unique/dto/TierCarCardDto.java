package com.carproject.unique.dto;

public record TierCarCardDto(
        Long trimId,
        Long modelId,
        String displayName,
        Long basePrice,

        // ✅ 추가
        String imageUrl
) {}
