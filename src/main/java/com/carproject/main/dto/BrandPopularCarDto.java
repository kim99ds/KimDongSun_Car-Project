package com.carproject.main.dto;

/**
 * 메인 - "브랜드 별 인기" 섹션에서 노출할 차량 카드(이미지 1장 + 좋아요)
 */
public record BrandPopularCarDto(
        Long modelId,
        String modelName,
        String imageUrl,
        Long likeCount
) {}
