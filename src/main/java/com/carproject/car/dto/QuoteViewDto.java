package com.carproject.car.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 견적서 조회 화면용 DTO
 *  - DB 저장(QUOTE/QUOTE_OPTION) 기준으로 표시할 이름/금액을 담는다.
 */
@Getter
@AllArgsConstructor
public class QuoteViewDto {

    private Long quoteId;

    // 이름으로 보여주기
    private String brandName;
    private String modelName;
    private String engineType;
    private String engineName;
    private String trimName;
    private String colorName;

    // ✅ 견적서 화면에 표시할 외관 이미지 (MODEL 기준, view_type='exterior')
    private String exteriorImageUrl;

    // 가격
    private BigDecimal basePrice;
    private BigDecimal colorPrice;
    private BigDecimal optionPrice;
    private BigDecimal discountPrice;
    private BigDecimal totalPrice;

    // 선택 옵션 라인
    private List<QuoteOptionLineDto> options;

    @Getter
    @AllArgsConstructor
    public static class QuoteOptionLineDto {
        private String optionName;
        private String optionType;              // PACKAGE / SINGLE
        private BigDecimal optionPrice;         // 패키지 포함이면 0
        private String includedByPackageName;   // 패키지 포함이면 패키지명, 아니면 null
    }
}
