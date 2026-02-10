package com.carproject.main.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BestCarCardDto {
    private Long modelId;
    private String brandName;
    private String modelName;
    private String imageUrl;   // 대표 이미지
    private Long likeCount;
}
