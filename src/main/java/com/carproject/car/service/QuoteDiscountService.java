package com.carproject.car.service;

import com.carproject.car.entity.Brand;
import com.carproject.car.entity.CarModel;
import com.carproject.car.entity.CarTrim;
import com.carproject.car.entity.CarVariant;
import com.carproject.event.entity.DiscountType;
import com.carproject.event.entity.Event;
import com.carproject.event.entity.EventPolicy;
import com.carproject.event.entity.EventStatus;
import com.carproject.event.entity.EventTarget;
import com.carproject.event.entity.TargetType;
import com.carproject.event.repository.EventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ✅ B안 적용:
 * - "여러 이벤트가 매칭되더라도" 견적에는 "가장 큰 할인 1개"만 적용
 * - 이벤트 1개 내부에서 정책(PRICE/RATE)이 여러 개면 그 중에서도 "가장 큰 할인 1개"만 선택
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteDiscountService {

    private final EventRepository eventRepository;

    public DiscountResult calculate(CarTrim trim, BigDecimal subtotal) {

        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (subtotal.signum() <= 0) {
            return new DiscountResult(BigDecimal.ZERO, List.of());
        }

        LocalDate today = LocalDate.now();

        // 1) 이벤트 + targets만 fetch (bag 1개만 fetch라 안전)
        List<Event> events =
                eventRepository.findActiveEventsWithTargets(today, EventStatus.ACTIVE);

        if (events == null || events.isEmpty()) {
            return new DiscountResult(BigDecimal.ZERO, List.of());
        }

        // 2) policies는 IN 쿼리로 한번에 가져오기
        List<Long> eventIds = events.stream()
                .map(Event::getEventId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, List<EventPolicy>> policiesByEventId = new HashMap<>();

        if (!eventIds.isEmpty()) {
            List<EventPolicy> policies = eventRepository.findPoliciesByEventIds(eventIds);
            for (EventPolicy p : policies) {
                if (p == null || p.getEvent() == null || p.getEvent().getEventId() == null) continue;
                policiesByEventId
                        .computeIfAbsent(p.getEvent().getEventId(), k -> new ArrayList<>())
                        .add(p);
            }
        }

        String brandName  = safeBrandName(trim);
        String segment    = safeSegment(trim);
        String engineType = safeEngineType(trim);

        // ✅ B안: 전체 이벤트 중 최대 할인 1개만 선택
        Long bestEventId = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (Event e : events) {

            List<EventTarget> targets = e.getTargets();
            if (targets == null || targets.isEmpty()) continue;

            if (!matchesTargets(targets, brandName, segment, engineType)) continue;

            List<EventPolicy> policies = policiesByEventId.getOrDefault(e.getEventId(), List.of());
            if (policies.isEmpty()) continue;

            BigDecimal bestForThisEvent = bestDiscountForEvent(subtotal, policies);
            if (bestForThisEvent.signum() <= 0) continue;

            if (bestForThisEvent.compareTo(bestDiscount) > 0) {
                bestDiscount = bestForThisEvent;
                bestEventId = e.getEventId();
            }
        }

        if (bestEventId == null || bestDiscount.signum() <= 0) {
            return new DiscountResult(BigDecimal.ZERO, List.of());
        }

        // cap
        if (bestDiscount.compareTo(subtotal) > 0) bestDiscount = subtotal;

        return new DiscountResult(
                bestDiscount,
                List.of(new AppliedEventDiscount(bestEventId, bestDiscount))
        );
    }

    // =========================================================
    // Matching
    // =========================================================
    private boolean matchesTargets(List<EventTarget> targets, String brandName, String segment, String engineType) {

        // ALL이면 무조건 적용
        boolean hasAll = targets.stream().anyMatch(t ->
                t.getTargetType() == TargetType.ALL
                        || "ALL".equalsIgnoreCase(nvlStr(t.getTargetValue()))
        );
        if (hasAll) return true;

        for (EventTarget t : targets) {
            if (t.getTargetType() == null) continue;

            String raw = nvlStr(t.getTargetValue());
            if (raw.isBlank()) continue;

            List<String> values = splitCsv(raw);

            switch (t.getTargetType()) {
                case BRAND -> {
                    if (equalsOneOf(brandName, values)) return true;
                }
                case CAR_TYPE -> {
                    // car_model.segment 를 CAR_TYPE으로 매칭
                    if (equalsOneOf(segment, values)) return true;
                }
                case FUEL -> {
                    if (equalsOneOf(engineType, values)) return true;
                }
                default -> {
                    // 그 외 타입은 현재 할인 적용에 사용하지 않음
                }
            }
        }
        return false;
    }

    // =========================================================
    // Policy -> discount
    // =========================================================
    private BigDecimal bestDiscountForEvent(BigDecimal subtotal, List<EventPolicy> policies) {

        BigDecimal best = BigDecimal.ZERO;

        for (EventPolicy p : policies) {
            if (p == null || p.getDiscountType() == null || p.getDiscountValue() == null) continue;

            BigDecimal d = BigDecimal.ZERO;

            if (p.getDiscountType() == DiscountType.PRICE) {
                d = p.getDiscountValue();
            } else if (p.getDiscountType() == DiscountType.RATE) {
                d = subtotal.multiply(p.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            }

            if (d.compareTo(best) > 0) best = d;
        }

        return best.max(BigDecimal.ZERO);
    }

    // =========================================================
    // Safe getters (NPE 방지)
    // =========================================================
    private String safeBrandName(CarTrim trim) {
        try {
            CarVariant v = trim != null ? trim.getVariant() : null;
            CarModel m = v != null ? v.getModel() : null;
            Brand b = m != null ? m.getBrand() : null;
            return b != null ? nvlStr(b.getBrandName()) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String safeSegment(CarTrim trim) {
        try {
            CarVariant v = trim != null ? trim.getVariant() : null;
            CarModel m = v != null ? v.getModel() : null;
            return m != null ? nvlStr(m.getSegment()) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String safeEngineType(CarTrim trim) {
        try {
            CarVariant v = trim != null ? trim.getVariant() : null;
            return v != null ? nvlStr(v.getEngineType()) : "";
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================
    // utils
    // =========================================================
    private static boolean equalsOneOf(String actual, List<String> candidates) {
        if (actual == null || actual.isBlank()) return false;
        if (candidates == null || candidates.isEmpty()) return false;

        for (String c : candidates) {
            if (c == null) continue;
            if (actual.equalsIgnoreCase(c.trim())) return true;
        }
        return false;
    }

    private static List<String> splitCsv(String raw) {
        if (raw == null) return List.of();
        return Arrays.stream(raw.split(","))
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static String nvlStr(String s) {
        return s == null ? "" : s.trim();
    }

    // =========================================================
    // DTOs
    // =========================================================
    public record AppliedEventDiscount(Long eventId, BigDecimal discountPrice) {}

    public record DiscountResult(BigDecimal totalDiscountPrice,
                                 List<AppliedEventDiscount> appliedEvents) {}
}
