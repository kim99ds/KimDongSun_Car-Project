package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class TrimDetailDto {

    private Long trimId;
    private String trimName;
    private BigDecimal basePrice;

    private List<TrimColorDto> colors;
    private List<OptionItemDto> options;
}
