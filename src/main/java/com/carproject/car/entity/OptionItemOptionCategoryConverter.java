package com.carproject.car.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OptionItemOptionCategoryConverter implements AttributeConverter<OptionCategory, String> {

    @Override
    public String convertToDatabaseColumn(OptionCategory attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case WHEEL -> "wheel";
            case ASSISTANT -> "assistant";
            case INTERIOR -> "interior";
            case SEATS -> "seats";
            case ETC -> "etc";
            default -> throw new IllegalArgumentException(
                    "OPTION_ITEM.OPTION_CATEGORY는 WHEEL/ASSISTANT/INTERIOR/SEATS/ETC만 허용: " + attribute
            );
        };
    }

    @Override
    public OptionCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return switch (dbData.toLowerCase()) {
            case "wheel" -> OptionCategory.WHEEL;
            case "assistant" -> OptionCategory.ASSISTANT;
            case "interior" -> OptionCategory.INTERIOR;
            case "seats" -> OptionCategory.SEATS;
            case "etc" -> OptionCategory.ETC;
            default -> throw new IllegalArgumentException("DB OPTION_CATEGORY 범위 밖: " + dbData);
        };
    }
}
