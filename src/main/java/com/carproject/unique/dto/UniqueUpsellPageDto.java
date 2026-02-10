package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UniqueUpsellPageDto {
    private List<UniqueUpsellCarDto> pickList;     // 초기 선택 리스트
    private Long selectedTrimId;                   // 현재 선택된 트림
    private Long addPrice;                         // 추가 금액
    private List<UniqueUpsellCarDto> candidates;   // 업셀 추천 결과
}
