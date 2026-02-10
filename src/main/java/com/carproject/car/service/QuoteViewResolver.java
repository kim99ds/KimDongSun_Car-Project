package com.carproject.car.service;

import com.carproject.car.dto.QuoteViewDto;
import com.carproject.car.dto.QuoteViewDto.QuoteOptionLineDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuoteViewResolver {

    public QuoteViewDto resolve(QuoteViewDto quote) {

        if (quote == null) {
            return null;
        }

        String brandName = nvl(quote.getBrandName(), "-");
        String modelName = nvl(quote.getModelName(), "-");
        String engineType = nvl(quote.getEngineType(), "-");
        String engineName = nvl(quote.getEngineName(), "-");
        String trimName = nvl(quote.getTrimName(), "-");
        String colorName = nvl(quote.getColorName(), "-");
        String exteriorImageUrl = quote.getExteriorImageUrl();

        BigDecimal basePrice = nvl(quote.getBasePrice());
        BigDecimal colorPrice = nvl(quote.getColorPrice());
        BigDecimal optionPrice = nvl(quote.getOptionPrice());
        BigDecimal discountPrice = nvl(quote.getDiscountPrice());
        BigDecimal totalPrice = nvl(quote.getTotalPrice());

        // ✅ 여기 핵심 수정
        List<QuoteOptionLineDto> options =
                quote.getOptions() == null ? List.of() : quote.getOptions();

        return new QuoteViewDto(
                quote.getQuoteId(),
                brandName,
                modelName,
                engineType,
                engineName,
                trimName,
                colorName,
                exteriorImageUrl,
                basePrice,
                colorPrice,
                optionPrice,
                discountPrice,
                totalPrice,
                options
        );
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
