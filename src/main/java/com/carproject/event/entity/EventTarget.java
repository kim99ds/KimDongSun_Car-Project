package com.carproject.event.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "EVENT_TARGET",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_EVENT_TARGET", columnNames = {"EVENT_ID", "TARGET_TYPE", "TARGET_VALUE"})
)
public class EventTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVENT_TARGET_GEN")
    @SequenceGenerator(name = "SEQ_EVENT_TARGET_GEN", sequenceName = "CAR_PROJECT.SEQ_EVENT_TARGET", allocationSize = 1)
    @Column(name = "EVENT_TARGET_ID")
    private Long eventTargetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_TYPE", nullable = false, length = 30)
    private TargetType targetType;

    @Column(name = "TARGET_VALUE", length = 100)
    private String targetValue;

    // DB default SYSDATE, keep read-only
    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
