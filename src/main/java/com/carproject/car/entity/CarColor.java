package com.carproject.car.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CAR_COLOR", schema = "CAR_PROJECT")
public class CarColor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAR_COLOR_GEN")
    @SequenceGenerator(name = "SEQ_CAR_COLOR_GEN", sequenceName = "CAR_PROJECT.SEQ_CAR_COLOR", allocationSize = 1)
    @Column(name = "COLOR_ID")
    private Long colorId;

    @Column(name = "COLOR_NAME", nullable = false, length = 100)
    private String colorName;

    @Column(name = "COLOR_CODE", length = 20)
    private String colorCode;

    @Column(name = "COLOR_PRICE", nullable = false)
    private BigDecimal colorPrice = BigDecimal.ZERO;

    @Column(name = "COLOR_TYPE", length = 30)
    private String colorType;

    @OneToMany(mappedBy = "color", fetch = FetchType.LAZY)
    private List<TrimColor> trimColors = new ArrayList<>();
}
