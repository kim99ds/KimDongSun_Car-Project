package com.carproject.unique.dto;

public record TrimSearchItemDto(
        Long trimId,
        Long modelId,
        String brandName,
        String modelName,
        Integer modelYear,
        String trimName,
        Long basePrice,

        // ✅ 추가: exterior 이미지 URL (없으면 null)
        String imageUrl
) {
    public String displayName() {
        String y = (modelYear == null) ? "" : (modelYear + " ");
        String t = (trimName == null || trimName.isBlank()) ? "" : (" " + trimName);
        return (y + brandName + " " + modelName + t).trim();
    }
}
