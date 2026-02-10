package com.carproject.event.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class EventDetailDto {

    private Long eventId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bannerImage;

    private String status;
    private String statusMessage;

    private boolean ended;

    private String targetDisplay;

    // ✅ 추가
    private List<EventCarItemDto> matchedCars;

    private List<String> targets;
    private List<String> policies;
}
