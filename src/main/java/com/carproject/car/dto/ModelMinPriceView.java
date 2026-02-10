package com.carproject.car.dto;

import java.math.BigDecimal;

/**
 * 모델별 최저 트림가 projection
 */
public interface ModelMinPriceView {
    Long getModelId();
    BigDecimal getMinBasePrice();
}
