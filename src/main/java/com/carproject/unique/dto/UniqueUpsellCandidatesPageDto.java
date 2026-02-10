package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UniqueUpsellCandidatesPageDto {

    // 선택한 차량
    private final UniqueUpsellCarCardDto picked;

    // 추천 차량들
    private final List<UniqueUpsellCarCardDto> candidates;

    // ✅ pick 화면과 이름 통일
    private final Long selectedAddPrice;

    // ✅ addPrice = 제한없음(-1)로 진입한 경우에만 true
    private final boolean unlimited;
}