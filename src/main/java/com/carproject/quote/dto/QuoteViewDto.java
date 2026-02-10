package com.carproject.quote.dto;

import java.math.BigDecimal;

public class QuoteViewDto {

    private String modelName;
    private String trimName;
    private BigDecimal totalPrice;

    // ✅ exterior 이미지 (MODEL 기준)
    private String exteriorImageUrl;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTrimName() {
        return trimName;
    }

    public void setTrimName(String trimName) {
        this.trimName = trimName;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getExteriorImageUrl() {
        return exteriorImageUrl;
    }

    public void setExteriorImageUrl(String exteriorImageUrl) {
        this.exteriorImageUrl = exteriorImageUrl;
    }
}
