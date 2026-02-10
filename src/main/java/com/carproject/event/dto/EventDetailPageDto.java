package com.carproject.event.dto;

import java.util.List;

public record EventDetailPageDto(
        EventDetailDto detail,
        List<EventNavItemDto> navItems // 종료 상세면 null
) {}
