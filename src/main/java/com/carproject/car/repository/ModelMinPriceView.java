package com.carproject.car.repository;

import java.math.BigDecimal;

public interface ModelMinPriceView {
    Long getModelId();
    BigDecimal getMinBasePrice();
}
