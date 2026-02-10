package com.carproject.car.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ForMeRecommendDto {

    private Long modelId;
    private String brandName;
    private String modelName;
    private String segment;

    // ✅ 이미지 URL (추가)
    private String imageUrl;

    private BigDecimal minBasePrice;

    /** 0~100 */
    private int score;

    /** score를 그대로 %로 사용 (0~100) */
    private int matchRate;

    public ForMeRecommendDto(
            Long modelId,
            String brandName,
            String modelName,
            String segment,
            String imageUrl,          // ✅ 여기!
            BigDecimal minBasePrice,
            int score,
            int matchRate
    ) {
        this.modelId = modelId;
        this.brandName = brandName;
        this.modelName = modelName;
        this.segment = segment;
        this.imageUrl = imageUrl;   // ✅ 여기!
        this.minBasePrice = minBasePrice;
        this.score = score;
        this.matchRate = matchRate;
    }
}
