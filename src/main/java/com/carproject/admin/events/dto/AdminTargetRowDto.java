package com.carproject.admin.events.dto;

public record AdminTargetRowDto(
        Long eventTargetId,
        String eventTitle,
        String targetType,
        String targetValue
) {}
