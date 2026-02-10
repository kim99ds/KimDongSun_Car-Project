package com.carproject.car.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ⚠️ 주의:
 * - 과거에 autoApply=true 로 되어 있으면 OptionCategory를 쓰는 모든 필드에 침투합니다.
 * - OPTION_ITEM.OPTION_CATEGORY는 DB 체크제약(소문자 5개) 때문에 이 컨버터를 쓰면 안 됩니다.
 * - OPTION_ITEM에는 OptionItemOptionCategoryConverter 를 @Convert로 "국소 적용"하세요.
 */
@Converter(autoApply = false)
public class OptionCategoryConverter implements AttributeConverter<OptionCategory, String> {

    @Override
    public String convertToDatabaseColumn(OptionCategory attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OptionCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        // 여기서 이상값을 ETC로 숨기지 말고, 바로 터뜨려서 데이터 꼬임을 드러내는 게 안전
        return OptionCategory.valueOf(dbData.toUpperCase());
    }
}
