package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CarTrimDto {

    private Long trimId;
    private String trimName;
    private BigDecimal basePrice;

    // CAR_TRIM.DESCRIPTION (트림 설명)
    private String description;
}
