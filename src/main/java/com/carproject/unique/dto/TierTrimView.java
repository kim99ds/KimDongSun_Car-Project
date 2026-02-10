package com.carproject.unique.dto;

public interface TierTrimView {
    Long getTrimId();
    Long getModelId();
    String getModelName();
    Long getBasePrice();

    // ✅ 추가
    String getImageUrl();
}
