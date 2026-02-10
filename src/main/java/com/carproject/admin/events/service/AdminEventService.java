package com.carproject.admin.events.service;

import com.carproject.admin.events.dto.AdminEventForms;
import com.carproject.admin.events.dto.AdminEventForms.EventCreateForm;
import com.carproject.admin.events.dto.AdminEventForms.EventForm;
import com.carproject.admin.events.dto.AdminEventForms.TargetForm;
import com.carproject.event.entity.*;
import com.carproject.event.repository.EventPolicyRepository;
import com.carproject.event.repository.EventRepository;
import com.carproject.event.repository.EventTargetRepository;
import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventTargetRepository eventTargetRepository;
    private final EventPolicyRepository eventPolicyRepository;
    private final MemberRepository memberRepository;

    @Value("${app.upload-dir:/uploads}")
    private String uploadDir;

    /* ===== LIST (SEARCH) ===== */
    public List<Event> findEvents(String q, EventStatus status, LocalDate from, LocalDate to) {
        String qq = blankToNull(q);
        return eventRepository.findAdminEventListFiltered(qq, status, from, to);
    }

    public List<EventTarget> findTargets(Long eventId, TargetType targetType, String targetValue) {
        String tv = blankToNull(targetValue);
        return eventTargetRepository.findAdminTargetListFiltered(eventId, targetType, tv);
    }

    public List<EventPolicy> findPolicies(String q, DiscountType type) {
        String qq = blankToNull(q);
        return eventPolicyRepository.findAdminPolicyListFiltered(qq, type);
    }

    // 기존 호출부 호환
    public List<Event> findEvents() { return findEvents(null, null, null, null); }
    public List<EventTarget> findTargets() { return findTargets(null, null, null); }
    public List<EventPolicy> findPolicies() { return findPolicies(null, null); }

    /* ===== GET ===== */
    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "이벤트 없음"));
    }

    public EventTarget getTargetOrThrow(Long targetId) {
        return eventTargetRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "타겟 없음"));
    }

    public List<EventTarget> getTargetsOf(Long eventId) {
        return eventTargetRepository.findAllByEvent_EventId(eventId);
    }

    public List<EventPolicy> getPoliciesOf(Long eventId) {
        return eventPolicyRepository.findAllByEvent_EventId(eventId);
    }

    public EventPolicy getPolicy(Long policyId) {
        return getPolicyOrThrow(policyId);
    }

    public EventPolicy getPolicyOrThrow(Long policyId) {
        return eventPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이벤트 정책 없음"));
    }

    /* ===== EVENT CRUD ===== */
    @Transactional
    public Long createEvent(EventForm f, MultipartFile bannerFile, Long memberId) {

        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "생성자 없음"));

        Event e = new Event();
        e.setTitle(f.getTitle());
        e.setDescription(f.getDescription());
        e.setStartDate(f.getStartDate());
        e.setEndDate(f.getEndDate());
        e.setStatus(f.getStatus());
        e.setCreatedBy(creator);

        if (bannerFile != null && !bannerFile.isEmpty()) {
            e.setBannerImage(saveBanner(bannerFile));
        }

        return eventRepository.save(e).getEventId();
    }

    @Transactional
    public Long createEventWithTargetsPolicies(EventCreateForm f, MultipartFile bannerFile, Long memberId) {

        Long eventId = createEvent(f, bannerFile, memberId);
        Event e = getEventOrThrow(eventId);

        if (f.getTargets() != null) {
            for (AdminEventForms.TargetForm tf : f.getTargets()) {
                if (tf == null || tf.getTargetType() == null) continue;

                EventTarget t = new EventTarget();
                t.setEvent(e);
                t.setTargetType(tf.getTargetType());
                t.setTargetValue(blankToNull(tf.getTargetValue()));
                eventTargetRepository.save(t);
            }
        }

        if (f.getPolicies() != null) {
            for (AdminEventForms.PolicyForm pf : f.getPolicies()) {
                if (pf == null || pf.getDiscountType() == null || pf.getDiscountValue() == null) continue;

                EventPolicy p = new EventPolicy();
                p.setEvent(e);
                p.setDiscountType(pf.getDiscountType());
                p.setDiscountValue(pf.getDiscountValue());
                eventPolicyRepository.save(p);
            }
        }

        return eventId;
    }

    @Transactional
    public void updateEvent(Long eventId, EventForm f, MultipartFile bannerFile) {

        Event e = getEventOrThrow(eventId);
        e.setTitle(f.getTitle());
        e.setDescription(f.getDescription());
        e.setStartDate(f.getStartDate());
        e.setEndDate(f.getEndDate());
        e.setStatus(f.getStatus());

        if (bannerFile != null && !bannerFile.isEmpty()) {
            e.setBannerImage(saveBanner(bannerFile));
        }
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        eventPolicyRepository.deleteByEvent_EventId(eventId);
        eventTargetRepository.deleteByEvent_EventId(eventId);
        eventRepository.deleteById(eventId);
    }

    /* ===== TARGET EDIT ===== */
    @Transactional
    public void updateTargetOnly(Long targetId, TargetForm f) {
        EventTarget t = getTargetOrThrow(targetId);
        if (f.getTargetType() != null) t.setTargetType(f.getTargetType());
        t.setTargetValue(blankToNull(f.getTargetValue()));
    }

    @Transactional
    public void deleteTarget(Long targetId) {
        eventTargetRepository.deleteById(targetId);
    }

    @Transactional
    public void deletePolicy(Long policyId) {
        eventPolicyRepository.deleteById(policyId);
    }

    /* ===== POLICY EDIT ===== */
    @Transactional
    public void updatePolicyOnly(Long policyId, AdminEventForms.PolicyForm form) {
        EventPolicy p = getPolicyOrThrow(policyId);
        if (form.getDiscountType() != null) p.setDiscountType(form.getDiscountType());
        if (form.getDiscountValue() != null) p.setDiscountValue(form.getDiscountValue());
    }

    /* ===== FILE SAVE ===== */
    private String saveBanner(MultipartFile file) {
        try {
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);

            String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "_" + UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + name;
        } catch (IOException e) {
            throw new RuntimeException("배너 저장 실패", e);
        }
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
