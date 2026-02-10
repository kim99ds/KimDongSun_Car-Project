package com.carproject.admin.events.controller;

import com.carproject.admin.events.dto.AdminEventForms;
import com.carproject.admin.events.dto.AdminEventForms.EventCreateForm;
import com.carproject.admin.events.dto.AdminEventForms.EventForm;
import com.carproject.admin.events.dto.AdminEventForms.TargetForm;
import com.carproject.admin.events.service.AdminEventService;
import com.carproject.event.entity.*;
import com.carproject.global.security.SecurityMemberResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/event_manage")
public class AdminEventManageController {

    private final AdminEventService service;
    private final SecurityMemberResolver securityMemberResolver;

    /* =====================
       LIST PAGE + SEARCH
       ===================== */
    @GetMapping
    public String page(@RequestParam(defaultValue = "events") String tab,
                       @RequestParam(value = "fragment", required = false) String fragment,

                       // events search
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) EventStatus status,
                       @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

                       // targets search
                       @RequestParam(value = "eventId", required = false) Long eventId,
                       @RequestParam(value = "targetType", required = false) TargetType targetType,
                       @RequestParam(value = "targetValue", required = false) String targetValue,

                       // policies search
                       @RequestParam(value = "type", required = false) DiscountType type,

                       Model model) {

        model.addAttribute("activeMenu", "events");
        model.addAttribute("tab", tab);

        // ✅ 검색값 유지(화면에 다시 바인딩)
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("eventId", eventId);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetValue", targetValue);
        model.addAttribute("type", type);

        switch (tab) {
            case "targets" -> model.addAttribute("targets", service.findTargets(eventId, targetType, targetValue));
            case "policies" -> model.addAttribute("policies", service.findPolicies(q, type));
            default -> model.addAttribute("events", service.findEvents(q, status, from, to));
        }

        model.addAttribute("contentTemplate", "admin/fragments/event_manage");

        if (fragment != null) {
            return "admin/fragments/event_manage :: content";
        }
        return "admin/app";
    }

    /* =====================
       EVENT CREATE
       ===================== */
    @GetMapping("/new")
    public String newForm(@RequestParam(value = "fragment", required = false) String fragment,
                          Model model) {

        model.addAttribute("activeMenu", "events");
        model.addAttribute("tab", "events");
        model.addAttribute("mode", "create");

        EventCreateForm form = new EventCreateForm();
        form.getTargets().add(new TargetForm());
        form.getPolicies().add(new AdminEventForms.PolicyForm());

        model.addAttribute("event", new Event());
        model.addAttribute("eventForm", form);

        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("targetTypes", TargetType.values());
        model.addAttribute("discountTypes", DiscountType.values());

        model.addAttribute("contentTemplate", "admin/fragments/event_upload");
        if (fragment != null) return "admin/fragments/event_upload :: content";
        return "admin/app";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("eventForm") EventCreateForm form,
                         BindingResult bindingResult,
                         @RequestParam(required = false) MultipartFile bannerFile,
                         @RequestParam(value = "fragment", required = false) String fragment,
                         Authentication authentication,
                         Model model) {

        // ✅ “전부 다 적어야 등록” : 배너 파일도 필수 처리
        if (bannerFile == null || bannerFile.isEmpty()) {
            bindingResult.reject("bannerFile", "필수 작성 사항입니다.");
            model.addAttribute("bannerFileError", "필수 작성 사항입니다.");
        }

        // ✅ 날짜 유효성
        if (form.getStartDate() != null && form.getEndDate() != null) {
            if (form.getEndDate().isBefore(form.getStartDate())) {
                bindingResult.rejectValue("endDate", "endDateBeforeStart", "필수 작성 사항입니다.");
            }
        }

        if (bindingResult.hasErrors()) {

            // ✅ [중요] POST 바인딩 결과로 리스트가 비면 targets[0]/policies[0]에서 템플릿 500 터짐 방지
            if (form.getTargets() == null || form.getTargets().isEmpty()) {
                form.getTargets().add(new TargetForm());
            }
            if (form.getPolicies() == null || form.getPolicies().isEmpty()) {
                form.getPolicies().add(new AdminEventForms.PolicyForm());
            }

            model.addAttribute("activeMenu", "events");
            model.addAttribute("tab", "events");
            model.addAttribute("mode", "create");
            model.addAttribute("eventForm", form);

            model.addAttribute("statuses", EventStatus.values());
            model.addAttribute("targetTypes", TargetType.values());
            model.addAttribute("discountTypes", DiscountType.values());

            model.addAttribute("contentTemplate", "admin/fragments/event_upload");

            if (fragment != null) return "admin/fragments/event_upload :: content";
            return "admin/app";
        }

        Long memberId = securityMemberResolver.requireMemberId(authentication);
        service.createEventWithTargetsPolicies(form, bannerFile, memberId);

        return "redirect:/admin/event_manage?tab=events";
    }

    /* =====================
       EVENT EDIT
       ===================== */
    @GetMapping("/{eventId}/edit")
    public String editForm(@PathVariable Long eventId,
                           @RequestParam(value = "fragment", required = false) String fragment,
                           Model model) {

        Event e = service.getEventOrThrow(eventId);

        model.addAttribute("activeMenu", "events");
        model.addAttribute("tab", "events");
        model.addAttribute("mode", "edit");
        model.addAttribute("event", e);
        model.addAttribute("eventForm", EventForm.from(e));
        model.addAttribute("statuses", EventStatus.values());

        model.addAttribute("contentTemplate", "admin/fragments/event_edit");
        if (fragment != null) return "admin/fragments/event_edit :: content";
        return "admin/app";
    }

    @PostMapping("/{eventId}")
    public String update(@PathVariable Long eventId,
                         @ModelAttribute EventForm form,
                         @RequestParam(required = false) MultipartFile bannerFile) {

        service.updateEvent(eventId, form, bannerFile);
        return "redirect:/admin/event_manage?tab=events";
    }

    @PostMapping("/{eventId}/delete")
    public String delete(@PathVariable Long eventId) {
        service.deleteEvent(eventId);
        return "redirect:/admin/event_manage?tab=events";
    }

    /* =====================
       TARGET EDIT
       ===================== */
    @GetMapping("/targets/{targetId}/edit")
    public String editTargetForm(@PathVariable Long targetId,
                                 @RequestParam(value = "fragment", required = false) String fragment,
                                 Model model) {

        EventTarget t = service.getTargetOrThrow(targetId);

        TargetForm form = new TargetForm();
        form.setTargetType(t.getTargetType());
        form.setTargetValue(t.getTargetValue());

        model.addAttribute("activeMenu", "events");
        model.addAttribute("tab", "targets");
        model.addAttribute("mode", "editTarget");
        model.addAttribute("target", t);
        model.addAttribute("targetForm", form);
        model.addAttribute("targetTypes", TargetType.values());

        model.addAttribute("contentTemplate", "admin/fragments/event_target_form");
        if (fragment != null) return "admin/fragments/event_target_form :: content";
        return "admin/app";
    }

    @PostMapping("/targets/{targetId}")
    public String updateTarget(@PathVariable Long targetId,
                               @ModelAttribute TargetForm form) {

        service.updateTargetOnly(targetId, form);
        return "redirect:/admin/event_manage?tab=targets";
    }

    @PostMapping("/targets/{targetId}/delete")
    public String deleteTarget(@PathVariable Long targetId) {
        service.deleteTarget(targetId);
        return "redirect:/admin/event_manage?tab=targets";
    }

    /* =====================
       POLICY: 조회(JSON) - 모달 값 채우기용
       ===================== */
    @GetMapping("/policies/{policyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPolicyJson(@PathVariable Long policyId) {

        var p = service.getPolicy(policyId);

        Map<String, Object> body = new HashMap<>();
        body.put("policyId", p.getEventPolicyId()); // 필드명은 엔티티에 맞춰 수정 필요할 수 있음
        body.put("eventId", (p.getEvent() != null ? p.getEvent().getEventId() : null));
        body.put("discountType", (p.getDiscountType() != null ? p.getDiscountType().name() : null));
        body.put("discountValue", (p.getDiscountValue() != null ? p.getDiscountValue().toPlainString() : null));

        return ResponseEntity.ok(body);
    }

    /* =====================
       POLICY EDIT / DELETE
       ===================== */
    @PostMapping("/policies/{policyId}")
    public String updatePolicy(@PathVariable Long policyId,
                               @ModelAttribute AdminEventForms.PolicyForm form) {

        // type이 누락될 수 있는 케이스 방어
        if (form.getDiscountType() == null) {
            var existing = service.getPolicy(policyId);
            form.setDiscountType(existing.getDiscountType());
        }

        // value가 빈 문자열로 넘어오면 null일 수 있음 → 기존값 유지(원하면 제거 가능)
        if (form.getDiscountValue() == null) {
            var existing = service.getPolicy(policyId);
            BigDecimal v = existing.getDiscountValue();
            form.setDiscountValue(v);
        }

        service.updatePolicyOnly(policyId, form);
        return "redirect:/admin/event_manage?tab=policies";
    }

    @PostMapping("/policies/{policyId}/delete")
    public String deletePolicy(@PathVariable Long policyId) {
        service.deletePolicy(policyId);
        return "redirect:/admin/event_manage?tab=policies";
    }
}
