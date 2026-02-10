package com.carproject.unique.dto;

public interface TrimSearchItemView {
    Long getTrimId();
    Long getModelId();
    String getBrandName();
    String getModelName();
    Integer getModelYear();
    String getTrimName();
    Long getBasePrice();

    // ✅ 추가
    String getImageUrl();
}
