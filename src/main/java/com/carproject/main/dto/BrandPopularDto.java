package com.carproject.main.dto;

import java.util.List;

/**
 * 메인 - "브랜드 별 인기" 섹션용 DTO
 */
public record BrandPopularDto(
        Long brandId,
        String brandName,
        String subtitle,
        List<BrandPopularCarDto> cars
) {}
