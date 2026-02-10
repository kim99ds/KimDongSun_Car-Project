package com.carproject.event.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Setter
@ToString
@NoArgsConstructor
@Builder
public class EventBannerDto {

    private Long eventId;
    private String title;
    private String bannerImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String statusMessage;

    // ✅ 추가: "브랜드: 현대, 기아 / 세그먼트: SUV, EV" 같은 표시용 문자열
    private String targetDisplay;
}
