package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CarVariantDto {

    private Long variantId;
    private String engineType;
    private String engineName;

    private List<CarTrimDto> trims;
}
