package com.carproject.event.entity;

import com.carproject.global.common.entity.BaseTimeEntity;
import com.carproject.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EVENT", schema = "CAR_PROJECT")
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVENT_GEN")
    @SequenceGenerator(name = "SEQ_EVENT_GEN", sequenceName = "CAR_PROJECT.SEQ_EVENT", allocationSize = 1)
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private EventStatus status = EventStatus.READY;

    @Column(name = "BANNER_IMAGE", length = 255)
    private String bannerImage;

    // =====================================================
    // ✅ Thymeleaf(admin/fragments/events/event_manage.html) 호환용 Getter
    // 템플릿에서 e.thumbnailUrl 을 사용 중인데, 엔티티에 해당 필드/Getter가 없으면 500 발생.
    // 현재 DB 컬럼은 BANNER_IMAGE 하나만 있으므로, 이를 썸네일로 그대로 노출한다.
    // =====================================================
    @Transient
    public String getThumbnailUrl() {
        return this.bannerImage;
    }

    // =====================================================
    // ✅ Thymeleaf 호환용 Getter (템플릿에서 e.eventCode 사용)
    // DB에 eventCode 컬럼이 없더라도 화면에서 "EVT_000123" 같은 코드 표시를 위해
    // eventId 기반으로 가상 코드를 만들어 제공한다.
    // =====================================================
    @Transient
    public String getEventCode() {
        if (this.eventId == null) return null;
        return "EVT_" + String.format("%06d", this.eventId);
    }

    // =====================================================
    // ✅ Thymeleaf 호환용 Getter (템플릿에서 e.plannerName 사용)
    // createdBy가 null인 케이스나 Lazy 로딩 문제로 화면이 깨지는 걸 방지한다.
    // =====================================================
    @Transient
    public String getPlannerName() {
        try {
            if (this.createdBy == null) return null;
            // Member 엔티티에 name/nickname/username 등 어떤 필드가 있든 getName() 우선
            return this.createdBy.getName();
        } catch (Exception e) {
            return null;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    private Member createdBy;

    // =====================================================
    // ✅ Thymeleaf(admin/fragments/events/event_manage.html) 호환용 Getter
    // 템플릿에서 e.plannerName 을 참조하는데, DB/엔티티에 컬럼이 없으면 500 발생.
    // createdBy(Member)의 name 을 노출해 UI(기획자/등록자 표시)를 유지한다.
    // =====================================================


    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<EventPolicy> policies = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<EventTarget> targets = new ArrayList<>();

    // =====================================================
    // ✅ Thymeleaf(admin/event_manage.html) 호환용 Getter
    // 템플릿에서 e.startAt / e.endAt 을 쓰는 경우 500 방지
    // (DB에는 START_DATE/END_DATE만 있으므로 LocalDateTime으로 변환 제공)
    // =====================================================

    @Transient
    public LocalDateTime getStartAt() {
        if (this.startDate == null) return null;
        return this.startDate.atStartOfDay(); // 00:00:00
    }

    @Transient
    public LocalDateTime getEndAt() {
        if (this.endDate == null) return null;
        return this.endDate.atTime(LocalTime.MAX); // 23:59:59.999999999
    }
    // =====================================================
    // ✅ Thymeleaf(admin/event_manage.html) 호환용 Getter
    // 템플릿에서 e.active 사용 중 → 엔티티에 없으면 500 발생
    // =====================================================
    @Transient
    public boolean isActive() {
        // 1) 상태 기반(가장 단순)
        // return this.status == EventStatus.ACTIVE;

        // 2) 상태 + 기간 기반(추천: 실제 "진행중" 의미)
        if (this.status != EventStatus.ACTIVE) return false;
        if (this.startDate == null || this.endDate == null) return false;

        LocalDate today = LocalDate.now();
        return (today.isEqual(startDate) || today.isAfter(startDate))
                && (today.isEqual(endDate) || today.isBefore(endDate));
    }

}
