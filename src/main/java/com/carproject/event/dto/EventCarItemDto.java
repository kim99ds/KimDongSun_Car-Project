package com.carproject.event.dto;

public record EventCarItemDto(
        Long modelId,
        String brandName,
        String modelName,
        Integer modelYear,
        String segment
) {}
