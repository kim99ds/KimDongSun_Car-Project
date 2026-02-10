package com.carproject.unique.dto;

import java.math.BigDecimal;

public interface TrimPickView {
    Long getTrimId();
    String getModelName();
    String getTrimName();
    BigDecimal getBasePrice();
}
