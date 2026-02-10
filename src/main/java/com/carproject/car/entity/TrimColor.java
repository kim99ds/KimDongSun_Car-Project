package com.carproject.car.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "TRIM_COLOR",
    schema = "CAR_PROJECT",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_TRIM_COLOR", columnNames = {"TRIM_ID", "COLOR_ID"}),
        @UniqueConstraint(name = "UK_TRIMCOLOR_TRIMID_TCUID", columnNames = {"TRIM_ID", "TRIM_COLOR_ID"})
    }
)
public class TrimColor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TRIM_COLOR_GEN")
    @SequenceGenerator(name = "SEQ_TRIM_COLOR_GEN", sequenceName = "CAR_PROJECT.SEQ_TRIM_COLOR", allocationSize = 1)
    @Column(name = "TRIM_COLOR_ID")
    private Long trimColorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRIM_ID", nullable = false)
    private CarTrim trim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COLOR_ID", nullable = false)
    private CarColor color;
}
