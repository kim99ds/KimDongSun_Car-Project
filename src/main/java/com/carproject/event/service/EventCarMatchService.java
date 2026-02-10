package com.carproject.event.service;

import com.carproject.car.repository.CarModelRepository;
import com.carproject.event.dto.EventCarItemDto;
import com.carproject.event.entity.EventTarget;
import com.carproject.event.repository.EventTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventCarMatchService {

    private static final int MAX_CARS = 12;

    private final EventTargetRepository eventTargetRepository;
    private final CarModelRepository carModelRepository;

    public List<EventCarItemDto> findMatchedCars(Long eventId) {
        List<EventTarget> targets = eventTargetRepository.findAllByEvent_EventId(eventId);

        if (targets == null || targets.isEmpty()) {
            return limit(carModelRepository.findRecommendedEventCars());
        }

        // ✅ 괄호로 우선순위 고정 (안전)
        boolean hasAll = targets.stream().anyMatch(t ->
                (t.getTargetType() != null && "ALL".equalsIgnoreCase(t.getTargetType().name()))
                        || "ALL".equalsIgnoreCase(nvl(t.getTargetValue()))
        );
        if (hasAll) {
            return limit(carModelRepository.findRecommendedEventCars());
        }

        Map<String, Set<String>> grouped = targets.stream()
                .filter(t -> t.getTargetType() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTargetType().name(),
                        Collectors.flatMapping(
                                t -> splitValues(t.getTargetValue()).stream(),
                                Collectors.toCollection(LinkedHashSet::new)
                        )
                ));

        List<String> brandNames  = toNullableList(grouped.get("BRAND"));
        List<String> segments    = toNullableList(firstNonEmpty(grouped.get("SEGMENT"), grouped.get("CAR_TYPE")));
        List<String> engineTypes = toNullableList(grouped.get("FUEL"));

        segments = expandSegments(segments);

        if ((brandNames == null || brandNames.isEmpty())
                && (segments == null || segments.isEmpty())
                && (engineTypes == null || engineTypes.isEmpty())) {
            return limit(carModelRepository.findRecommendedEventCars());
        }

        return limit(carModelRepository.findEventMatchedCars(brandNames, segments, engineTypes));
    }

    private static Set<String> firstNonEmpty(Set<String> a, Set<String> b) {
        if (a != null && !a.isEmpty()) return a;
        return (b != null && !b.isEmpty()) ? b : null;
    }

    private static List<String> toNullableList(Set<String> set) {
        if (set == null || set.isEmpty()) return null;
        return new ArrayList<>(set);
    }

    private static List<String> splitValues(String raw) {
        if (raw == null) return List.of();
        return Arrays.stream(raw.split(","))
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }

    private static List<String> expandSegments(List<String> segments) {
        if (segments == null || segments.isEmpty()) return segments;

        Set<String> out = new LinkedHashSet<>(segments);
        boolean hasSedan = segments.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(s -> "SEDAN".equalsIgnoreCase(s));

        if (hasSedan) out.add("Luxury Sedan");
        return new ArrayList<>(out);
    }

    private static List<EventCarItemDto> limit(List<EventCarItemDto> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.size() <= MAX_CARS ? list : list.subList(0, MAX_CARS);
    }
}
