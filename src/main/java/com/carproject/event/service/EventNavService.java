package com.carproject.event.service;

import com.carproject.event.dto.EventNavItemDto;
import com.carproject.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventNavService {

    private final EventRepository eventRepository;

    public List<EventNavItemDto> getOngoingNavItems(Long currentEventId) {
        return eventRepository.findOngoingNavItems(currentEventId).stream()
                .filter(p -> p != null && p.getEventId() != null && p.getTitle() != null && !p.getTitle().isBlank())
                .map(p -> new EventNavItemDto(p.getEventId(), p.getTitle()))
                .toList();
    }
}
