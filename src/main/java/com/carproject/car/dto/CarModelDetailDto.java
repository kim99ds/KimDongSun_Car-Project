package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CarModelDetailDto {

    private Long modelId;
    private String modelName;
    private String segment;
    private String brandName;

    private Long viewCount;
    private Long likeCount;

    private List<CarVariantDto> variants;
}
