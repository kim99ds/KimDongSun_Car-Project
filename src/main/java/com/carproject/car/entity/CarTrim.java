package com.carproject.car.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CAR_TRIM", schema = "CAR_PROJECT")
public class CarTrim {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAR_TRIM_GEN")
    @SequenceGenerator(name = "SEQ_CAR_TRIM_GEN", sequenceName = "CAR_PROJECT.SEQ_CAR_TRIM", allocationSize = 1)
    @Column(name = "TRIM_ID")
    private Long trimId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VARIANT_ID", nullable = false)
    private CarVariant variant;

    @Column(name = "TRIM_NAME", nullable = false, length = 100)
    private String trimName;

    @Column(name = "BASE_PRICE", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    // 트림 컬러
    @OneToMany(mappedBy = "trim", fetch = FetchType.LAZY)
    private Set<TrimColor> trimColors = new HashSet<>();

    // 트림 옵션
    @OneToMany(mappedBy = "trim", fetch = FetchType.LAZY)
    private Set<TrimOption> trimOptions = new HashSet<>();


}
