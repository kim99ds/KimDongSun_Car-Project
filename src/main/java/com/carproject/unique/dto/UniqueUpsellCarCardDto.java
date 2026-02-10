package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UniqueUpsellCarCardDto {

    private Long trimId;

    // ✅ 상세페이지 이동을 위해 필요 (/cars/{modelId})
    private Long modelId;

    private String brandName;
    private String modelName;
    private String trimName;
    private Long basePrice;
    private String imageUrl;
}