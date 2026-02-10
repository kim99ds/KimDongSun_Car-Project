package com.carproject.unique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UniqueUpsellPickPageDto {

    private List<BrandOptionDto> brands;

    // ✅ 다중 선택 브랜드
    private List<String> selectedBrands;

    // ✅ 세그먼트 옵션/선택값 추가
    private List<String> segments;
    private List<String> selectedSegments;

    private List<ModelOptionDto> models;
    private Long selectedModelId;

    // ✅ pick.html에서 쓰는 이름 유지
    private Long selectedAddPrice;

    // ✅ 검색어(템플릿에서 page.keyword 사용)
    private String keyword;

    private List<UniqueUpsellCarCardDto> cards;

    // ✅ paging
    private int page;        // 1-based
    private int totalPages;
    private long totalItems;
    private int pageSize;
}