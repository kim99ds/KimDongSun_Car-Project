package com.carproject.event.controller;

import com.carproject.event.dto.EventCarItemDto;
import com.carproject.event.dto.EventDetailDto;
import com.carproject.event.dto.EventNavItemDto;
import com.carproject.event.service.EventBannerService;
import com.carproject.event.service.EventCarMatchService;
import com.carproject.event.service.EventDetailService;
import com.carproject.event.service.EventNavService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventBannerService eventBannerService;
    private final EventDetailService eventDetailService;
    private final EventCarMatchService eventCarMatchService;

    // ✅ 추가
    private final EventNavService eventNavService;

    @GetMapping("/event")
    public String eventPage(@RequestParam(value = "tab", defaultValue = "ongoing") String tab,
                            Model model) {
        model.addAttribute("eventBanners", eventBannerService.getOngoingBanners());
        model.addAttribute("endedEventBanners", eventBannerService.getEndedBanners());
        model.addAttribute("tab", tab);
        return "event/index";
    }

    @GetMapping("/event/{eventId}")
    public String detail(@PathVariable Long eventId,
                         @RequestParam(defaultValue = "ongoing") String tab,
                         Model model) {

        EventDetailDto detail = eventDetailService.getDetail(eventId);

        List<EventCarItemDto> cars = eventCarMatchService.findMatchedCars(eventId);
        detail.setMatchedCars(cars);

        model.addAttribute("event", detail);

        // ✅ 핵심: 진행중 탭에서만 navItems 채움 (종료면 null 유지)
        List<EventNavItemDto> navItems = null;
        if ("ongoing".equalsIgnoreCase(tab)) {
            navItems = eventNavService.getOngoingNavItems(eventId);
        }
        model.addAttribute("navItems", navItems);

        model.addAttribute("tab", tab);
        return "event/detail";
    }
}
