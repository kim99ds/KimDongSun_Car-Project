package com.carproject.car.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuoteRequestDto {
    private Long modelId;
    private Long variantId;
    private Long trimId;
    private Long trimColorId;

    private List<Long> packageOptionIds;
    private List<Long> singleOptionIds;

    private Long totalPrice; // 프론트 계산값(참고용)
}
