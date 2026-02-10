package com.carproject.event.service;

import com.carproject.event.dto.EventBannerDto;
import com.carproject.event.entity.Event;
import com.carproject.event.entity.EventStatus;
import com.carproject.event.repository.EventRepository;
import com.carproject.event.repository.EventTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventBannerService {

    private final EventRepository eventRepository;
    private final EventTargetRepository eventTargetRepository; // ✅ 추가

    public List<EventBannerDto> getOngoingBanners() {
        List<Event> events = eventRepository.findOngoingEventsForBanner();
        return toDtosWithTargets(events);
    }

    public List<EventBannerDto> getEndedBanners() {
        List<Event> events = eventRepository.findEndedEventsForBanner();
        return toDtosWithTargets(events);
    }

    private List<EventBannerDto> toDtosWithTargets(List<Event> events) {
        if (events == null || events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getEventId)
                .filter(Objects::nonNull)
                .toList();

        // eventId -> "현대, 기아" 같은 문자열
        Map<Long, String> targetMap = loadTargetDisplayMap(eventIds);

        return events.stream()
                .map(e -> toDto(e, targetMap.get(e.getEventId())))
                .toList();
    }

    private Map<Long, String> loadTargetDisplayMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Map.of();

        List<Object[]> rows = eventTargetRepository.findBannerTargetDisplays(eventIds);

        Map<Long, String> map = new HashMap<>();
        for (Object[] r : rows) {
            Long eventId = r[0] == null ? null : ((Number) r[0]).longValue();
            String valuesAgg = (String) r[1];
            if (eventId != null && valuesAgg != null && !valuesAgg.isBlank()) {
                map.put(eventId, valuesAgg);
            }
        }
        return map;
    }

    private EventBannerDto toDto(Event e, String targetDisplay) {
        EventStatus status = e.getStatus();

        return EventBannerDto.builder()
                .eventId(e.getEventId())
                .title(e.getTitle())
                .bannerImage(e.getBannerImage())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())

                .status(status != null ? status.name() : null)
                .statusMessage(resolveStatusMessage(status))

                // ✅ 핵심: 주황 라벨에 찍힐 값
                .targetDisplay((targetDisplay == null || targetDisplay.isBlank()) ? "ALL" : targetDisplay)
                .build();
    }

    private String resolveStatusMessage(EventStatus status) {
        if (status == null) return "준비중";

        return switch (status) {
            case ACTIVE -> null;
            case INACTIVE -> "조기 마감되었습니다.";
            case END -> "종료된 이벤트입니다.";
            case READY -> "준비중";
        };
    }
}
