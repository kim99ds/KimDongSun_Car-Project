package com.carproject.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeStatusDto {
    private Long modelId;
    private Long memberId;
    private boolean liked;
    private Long likeCount;
}
