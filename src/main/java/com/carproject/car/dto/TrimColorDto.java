package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TrimColorDto {

    private Long trimColorId;
    private String colorName;
    private BigDecimal colorPrice;
}
