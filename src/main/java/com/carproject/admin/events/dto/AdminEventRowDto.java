package com.carproject.admin.events.dto;

public record AdminEventRowDto(
        Long eventId,
        String title,
        String period,
        String status
) {}
