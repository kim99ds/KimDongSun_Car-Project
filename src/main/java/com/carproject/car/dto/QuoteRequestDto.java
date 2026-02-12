package com.carproject.car.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

import java.util.List;

@Getter
@Setter
public class QuoteRequestDto {
    private Long modelId;
    private Long variantId;

    @NotNull(message = "trimId는 필수입니다.")
    @Positive(message = "trimId는 양수여야 합니다.")
    private Long trimId;

    @NotNull(message = "trimColorId는 필수입니다.")
    @Positive(message = "trimColorId는 양수여야 합니다.")
    private Long trimColorId;

    private List<Long> packageOptionIds;
    private List<Long> singleOptionIds;

    private Long totalPrice;
}
