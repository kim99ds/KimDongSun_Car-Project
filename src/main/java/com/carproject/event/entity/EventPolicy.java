package com.carproject.event.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EVENT_POLICY", schema = "CAR_PROJECT")
public class EventPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVENT_POLICY_GEN")
    @SequenceGenerator(name = "SEQ_EVENT_POLICY_GEN", sequenceName = "CAR_PROJECT.SEQ_EVENT_POLICY", allocationSize = 1)
    @Column(name = "EVENT_POLICY_ID")
    private Long eventPolicyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "DISCOUNT_TYPE", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "DISCOUNT_VALUE", nullable = false)
    private BigDecimal discountValue;
}
