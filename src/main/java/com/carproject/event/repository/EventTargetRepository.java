package com.carproject.event.repository;

import com.carproject.event.entity.EventTarget;
import com.carproject.event.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventTargetRepository extends JpaRepository<EventTarget, Long> {

    List<EventTarget> findAllByEvent_EventId(Long eventId);

    @Query(value = """
        SELECT 
            TARGET_TYPE,
            LISTAGG(TARGET_VALUE, ', ') WITHIN GROUP (ORDER BY TARGET_VALUE) AS VALUES_AGG
        FROM CAR_PROJECT.EVENT_TARGET
        WHERE EVENT_ID = :eventId
        GROUP BY TARGET_TYPE
        """, nativeQuery = true)
    List<Object[]> findGroupedTargetsByEventId(@Param("eventId") Long eventId);

    // ✅ 이벤트 목록 라벨용: eventId별 target_value를 LISTAGG로 합침
    @Query(value = """
        SELECT
          EVENT_ID,
          LISTAGG(TARGET_VALUE, ', ') WITHIN GROUP (ORDER BY TARGET_VALUE) AS VALUES_AGG
        FROM CAR_PROJECT.EVENT_TARGET
        WHERE EVENT_ID IN (:eventIds)
          AND TARGET_VALUE IS NOT NULL
          AND TARGET_TYPE IN ('BRAND','FUEL','CAR_TYPE')
        GROUP BY EVENT_ID
        """, nativeQuery = true)
    List<Object[]> findBannerTargetDisplays(@Param("eventIds") List<Long> eventIds);

    /* =========================
      [ADMIN] 이벤트 타겟 관리
      ========================= */
    @Query("""
        select t
        from EventTarget t
        join fetch t.event e
        order by e.createdAt desc, t.eventTargetId asc
    """)
    List<EventTarget> findAdminTargetList();

    @Query("""
        select t
        from EventTarget t
        join fetch t.event e
        where (:eventId is null or e.eventId = :eventId)
          and (:targetType is null or t.targetType = :targetType)
          and (:targetValue is null or lower(t.targetValue) like lower(concat('%', :targetValue, '%')))
        order by e.createdAt desc, t.eventTargetId asc
    """)
    List<EventTarget> findAdminTargetListFiltered(@Param("eventId") Long eventId,
                                                  @Param("targetType") TargetType targetType,
                                                  @Param("targetValue") String targetValue);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EventTarget t where t.event.eventId = :eventId")
    void deleteByEvent_EventId(@Param("eventId") Long eventId);
}
