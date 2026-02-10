package com.carproject.event.repository;

import com.carproject.event.entity.DiscountType;
import com.carproject.event.entity.EventPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventPolicyRepository extends JpaRepository<EventPolicy, Long> {

    List<EventPolicy> findAllByEvent_EventId(Long eventId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EventPolicy p where p.event.eventId = :eventId")
    void deleteByEvent_EventId(@Param("eventId") Long eventId);

    @Query("""
        select p
        from EventPolicy p
        join fetch p.event e
        order by e.createdAt desc, p.eventPolicyId asc
    """)
    List<EventPolicy> findAdminPolicyList();

    @Query("""
        select p
        from EventPolicy p
        join fetch p.event e
        where (:type is null or p.discountType = :type)
          and (
                :q is null
                or concat('', p.eventPolicyId) like concat('%', :q, '%')
                or concat('', e.eventId) like concat('%', :q, '%')
              )
        order by e.createdAt desc, p.eventPolicyId asc
    """)
    List<EventPolicy> findAdminPolicyListFiltered(@Param("q") String q,
                                                  @Param("type") DiscountType type);
}
