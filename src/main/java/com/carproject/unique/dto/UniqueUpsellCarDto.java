package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UniqueUpsellCarDto {
    private Long trimId;
    private String brandName;
    private String modelName;
    private String trimName;
    private Long basePrice;
    private String imageUrl;
}
