package com.carproject.event.service;

import com.carproject.event.dto.EventCarItemDto;
import com.carproject.event.dto.EventDetailDto;
import com.carproject.event.entity.DiscountType;
import com.carproject.event.entity.Event;
import com.carproject.event.entity.EventPolicy;
import com.carproject.event.repository.EventRepository;
import com.carproject.event.repository.EventTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventDetailService {

    private final EventRepository eventRepository;
    private final EventTargetRepository eventTargetRepository;
    private final EventCarMatchService eventCarMatchService;

    // ✅ 컨트롤러에서 이거 하나만 쓰면 됨
    public EventDetailDto getDetail(Long eventId) {
        return buildDetailDto(eventId, LocalDate.now());
    }

    private EventDetailDto buildDetailDto(Long eventId, LocalDate today) {
        Event eTargets = eventRepository.findDetailWithTargetsByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음: " + eventId));

        Event ePolicies = eventRepository.findDetailWithPoliciesByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 없음: " + eventId));

        String status = (eTargets.getStatus() != null) ? eTargets.getStatus().name() : null;

        boolean ended = isEnded(status, eTargets.getEndDate(), today);
        String statusMessage = resolveStatusMessage(status, ended, eTargets.getStartDate(), eTargets.getEndDate(), today);

        String targetDisplay = formatTargetDisplay(eventTargetRepository.findGroupedTargetsByEventId(eventId));
        List<EventCarItemDto> matchedCars = eventCarMatchService.findMatchedCars(eventId);

        // ✅ 핵심: EVENT_POLICY → 화면 표시용 문자열로 변환
        List<String> policyTexts = formatPolicies(ePolicies.getPolicies());

        return EventDetailDto.builder()
                .eventId(eTargets.getEventId())
                .title(eTargets.getTitle())
                .description(eTargets.getDescription())
                .startDate(eTargets.getStartDate())
                .endDate(eTargets.getEndDate())
                .bannerImage(eTargets.getBannerImage())
                .status(status)
                .statusMessage(statusMessage)
                .ended(ended)
                .targetDisplay(targetDisplay)
                .matchedCars(matchedCars)
                // targets/policies는 기존 필드 유지 (targets는 현재 화면에서 직접 안 써도 유지)
                .targets(eTargets.getTargets() == null ? List.of() : eTargets.getTargets().stream().map(String::valueOf).toList())
                .policies(policyTexts)
                .build();
    }

    private List<String> formatPolicies(List<EventPolicy> policies) {
        if (policies == null || policies.isEmpty()) return List.of();

        List<String> out = new ArrayList<>();
        for (EventPolicy p : policies) {
            if (p == null) continue;
            String text = formatPolicyText(p.getDiscountType(), p.getDiscountValue());
            if (text != null && !text.isBlank()) out.add(text);
        }
        return out;
    }

    private String formatPolicyText(DiscountType type, BigDecimal value) {
        if (type == null || value == null) return null;

        // BigDecimal → 정수로 안전 변환(임시 데이터가 정수 기준이므로)
        long v;
        try {
            v = value.longValue();
        } catch (Exception e) {
            return null;
        }

        if (type == DiscountType.RATE) {
            // 예: 10% 할인
            return v + "% 할인";
        }

        if (type == DiscountType.PRICE) {
            // 예: 1,500,000원 할인
            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            return nf.format(v) + "원 할인";
        }

        return null;
    }

    private String formatTargetDisplay(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) return "ALL";

        // type -> "현대, 기아" 같은 집계 문자열
        Map<String, String> grouped = new HashMap<>();
        for (Object[] r : rows) {
            String type = (String) r[0];
            String valuesAgg = (String) r[1];
            if (type != null && valuesAgg != null && !valuesAgg.isBlank()) {
                grouped.put(type, valuesAgg);
            }
        }

        // ✅ ALL 처리
        if (grouped.containsKey("ALL")) return "ALL";
        for (String v : grouped.values()) {
            if (v != null && v.toUpperCase().contains("ALL")) return "ALL";
        }

        // ✅ 원하는 출력: "현대, SUV" 처럼 값만
        List<String> tokens = new ArrayList<>();

        // 1) 브랜드 먼저
        addTokens(tokens, grouped.get("BRAND"));

        // 2) 세그먼트(SEGMENT) 없으면 CAR_TYPE 사용
        String seg = (grouped.get("SEGMENT") != null && !grouped.get("SEGMENT").isBlank())
                ? grouped.get("SEGMENT")
                : grouped.get("CAR_TYPE");
        addTokens(tokens, seg);

        // 3) 그 외 타입(FUEL 등)
        for (Map.Entry<String, String> entry : grouped.entrySet()) {
            String type = entry.getKey();
            if ("BRAND".equals(type) || "SEGMENT".equals(type) || "CAR_TYPE".equals(type) || "ALL".equals(type)) continue;
            addTokens(tokens, entry.getValue());
        }

        LinkedHashSet<String> uniq = new LinkedHashSet<>(tokens);
        return uniq.isEmpty() ? "ALL" : String.join(", ", uniq);
    }

    private void addTokens(List<String> out, String agg) {
        if (agg == null || agg.isBlank()) return;

        String[] parts = agg.split(",");
        for (String p : parts) {
            if (p == null) continue;
            String s = p.trim();
            if (!s.isBlank()) out.add(s);
        }
    }

    private boolean isEnded(String status, LocalDate endDate, LocalDate today) {
        if (status == null) return false;
        if ("INACTIVE".equals(status) || "END".equals(status)) return true;

        return "ACTIVE".equals(status)
                && endDate != null
                && endDate.isBefore(today);
    }

    private String resolveStatusMessage(String status,
                                        boolean ended,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        LocalDate today) {

        if (status == null) return "준비중";
        if ("INACTIVE".equals(status)) return "조기 마감되었습니다.";
        if (ended) return "종료된 이벤트입니다.";
        if ("ACTIVE".equals(status)) return "진행중";
        return "준비중";
    }
}
