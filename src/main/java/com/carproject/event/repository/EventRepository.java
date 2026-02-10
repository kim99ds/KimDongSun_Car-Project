package com.carproject.event.repository;

import com.carproject.event.entity.Event;
import com.carproject.event.entity.EventPolicy; // 추가
import com.carproject.event.entity.EventStatus; // 추가
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate; // 추가
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    /* =========================================================
       [추가] QuoteDiscountService 컴파일 에러 해결용 메서드
       ========================================================= */

    /**
     * ✅ 서비스 45행: 활성 상태이며 기간 내인 이벤트 + Targets 한 번에 조회
     */
    @EntityGraph(attributePaths = {"targets"})
    @Query("SELECT e FROM Event e WHERE e.status = :status " +
            "AND e.startDate <= :today AND (e.endDate IS NULL OR e.endDate >= :today)")
    List<Event> findActiveEventsWithTargets(@Param("today") LocalDate today, @Param("status") EventStatus status);

    /**
     * ✅ 서비스 60행: 이벤트 ID 목록으로 연관된 모든 정책(Policies) 조회
     */
    @Query("SELECT p FROM Event e JOIN e.policies p WHERE e.eventId IN :eventIds")
    List<EventPolicy> findPoliciesByEventIds(@Param("eventIds") List<Long> eventIds);


    /* =========================================================
       기존 메서드 유지
       ========================================================= */

    /**
     * ✅ 배너용 진행중 이벤트
     */
    @Query(value = """
        SELECT e.*
          FROM EVENT e
         WHERE e.STATUS = 'ACTIVE'
           AND e.START_DATE <= SYSDATE
           AND e.END_DATE   >= SYSDATE
           AND EXISTS (
               SELECT 1
                 FROM EVENT_TARGET t
                WHERE t.EVENT_ID = e.EVENT_ID
                  AND (
                        (t.TARGET_TYPE = 'ALL' AND (t.TARGET_VALUE = 'ALL' OR t.TARGET_VALUE IS NULL))
                     OR t.TARGET_TYPE IN ('BRAND', 'CAR_TYPE', 'FUEL')
                  )
           )
         ORDER BY e.START_DATE DESC
        """, nativeQuery = true)
    List<Event> findOngoingEventsForBanner();

    /**
     * ✅ 배너용 종료 이벤트
     */
    @Query(value = """
        SELECT e.*
          FROM EVENT e
         WHERE (
                e.STATUS IN ('INACTIVE','END')
                OR (e.STATUS = 'ACTIVE' AND e.END_DATE < SYSDATE)
               )
           AND EXISTS (
               SELECT 1
                 FROM EVENT_TARGET t
                WHERE t.EVENT_ID = e.EVENT_ID
                  AND (
                        (t.TARGET_TYPE = 'ALL' AND (t.TARGET_VALUE = 'ALL' OR t.TARGET_VALUE IS NULL))
                     OR t.TARGET_TYPE IN ('BRAND', 'CAR_TYPE', 'FUEL')
                  )
           )
         ORDER BY e.END_DATE DESC
        """, nativeQuery = true)
    List<Event> findEndedEventsForBanner();

    // ✅ 상세: targets만 fetch
    @EntityGraph(attributePaths = {"targets"})
    Optional<Event> findDetailWithTargetsByEventId(Long eventId);

    // ✅ 상세: policies만 fetch
    @EntityGraph(attributePaths = {"policies"})
    Optional<Event> findDetailWithPoliciesByEventId(Long eventId);

    /**
     * ✅ 진행중 상세 사이드 네비게이션용 (현재 이벤트 제외)
     */
    @Query(value = """
        select
            e.event_id as "eventId",
            e.title    as "title"
        from event e
        where e.status = 'ACTIVE'
          and (:currentEventId is null or e.event_id <> :currentEventId)
          and (e.start_date is null or e.start_date <= trunc(sysdate))
          and (e.end_date   is null or e.end_date   >= trunc(sysdate))
          and e.event_id is not null
          and e.title is not null
        order by e.start_date desc nulls last, e.event_id desc
        fetch first 8 rows only
        """, nativeQuery = true)
    List<EventNavItemProjection> findOngoingNavItems(@Param("currentEventId") Long currentEventId);

    interface EventNavItemProjection {
        Long getEventId();
        String getTitle();
    }

    /* =========================
       [ADMIN] 이벤트 관리 목록 (⭐ createdBy fetch로 500 방지)
       ========================= */
    @Query("""
        select e
        from Event e
        join fetch e.createdBy m
        order by e.createdAt desc
    """)
    List<Event> findAdminEventList();

    @Query("""
        select e
        from Event e
        where (:status is null or e.status = :status)
          and (
                :q is null
                or lower(e.title) like lower(concat('%', :q, '%'))
                or concat('', e.eventId) like concat('%', :q, '%')
              )
          and (:from is null or e.startDate >= :from)
          and (:to is null or e.endDate <= :to)
        order by e.createdAt desc
    """)
    List<Event> findAdminEventListFiltered(@Param("q") String q,
                                           @Param("status") EventStatus status,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);


}