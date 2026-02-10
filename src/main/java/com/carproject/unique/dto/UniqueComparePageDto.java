package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UniqueComparePageDto {
    private UniqueUpsellCarCardDto left;
    private UniqueUpsellCarCardDto right;
}
