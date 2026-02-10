package com.carproject.global.converter;

import com.carproject.car.dto.BudgetRange;
import org.springframework.core.convert.converter.Converter;

/**
 * 맞춤견적(For-me) 설문에서 전달되는 예산 값 변환기.
 *
 * UI(range input)는 "2000"~"7000"(step=1000) 형태로 값을 전송한다.
 * 스프링 기본 enum 바인딩은 enum 이름(UNDER_2000 등)만 지원하므로,
 * 문자열 숫자("2000")를 BudgetRange로 변환해준다.
 */
public class BudgetRangeConverter implements Converter<String, BudgetRange> {

    @Override
    public BudgetRange convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }

        // "2000"은 2천만원, "7000"은 7천만원 이상(OVER_7000)으로 해석
        int v;
        try {
            v = Integer.parseInt(source.trim());
        } catch (NumberFormatException e) {
            // 혹시 enum 이름으로 오는 경우도 대비(예: UNDER_2000)
            try {
                return BudgetRange.valueOf(source.trim());
            } catch (Exception ignored) {
                return null;
            }
        }

        return switch (v) {
            case 2000 -> BudgetRange.UNDER_2000;
            case 3000 -> BudgetRange.BETWEEN_2000_3000;
            case 4000 -> BudgetRange.BETWEEN_3000_4000;
            case 5000 -> BudgetRange.BETWEEN_4000_5000;
            case 6000 -> BudgetRange.BETWEEN_5000_7000;
            case 7000 -> BudgetRange.OVER_7000;
            default -> {
                // 안전장치: 2000 미만은 UNDER_2000, 7000 초과는 OVER_7000
                if (v < 2000) yield BudgetRange.UNDER_2000;
                if (v > 7000) yield BudgetRange.OVER_7000;
                // 중간값이 들어오면 가장 가까운 구간으로 매핑
                if (v < 3000) yield BudgetRange.UNDER_2000;
                if (v < 4000) yield BudgetRange.BETWEEN_2000_3000;
                if (v < 5000) yield BudgetRange.BETWEEN_3000_4000;
                if (v < 6000) yield BudgetRange.BETWEEN_4000_5000;
                yield BudgetRange.BETWEEN_5000_7000;
            }
        };
    }
}
