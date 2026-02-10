package com.carproject.admin.events.dto;

public record AdminPolicyRowDto(
        Long eventPolicyId,
        String eventTitle,
        String discountType,
        int discountValue,
        String displayText
) {}
