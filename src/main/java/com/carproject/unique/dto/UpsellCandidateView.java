package com.carproject.unique.dto;

import java.math.BigDecimal;

public interface UpsellCandidateView {
    Long getTrimId();
    String getModelName();
    String getTrimName();
    BigDecimal getBasePrice();
}
