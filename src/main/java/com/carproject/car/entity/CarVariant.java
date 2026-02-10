package com.carproject.car.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CAR_VARIANT", schema = "CAR_PROJECT")
public class CarVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAR_VARIANT_GEN")
    @SequenceGenerator(name = "SEQ_CAR_VARIANT_GEN", sequenceName = "CAR_PROJECT.SEQ_CAR_VARIANT", allocationSize = 1)
    @Column(name = "VARIANT_ID")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MODEL_ID", nullable = false)
    private CarModel model;

    @Column(name = "ENGINE_TYPE", length = 100)
    private String engineType;

    @Column(name = "ENGINE_NAME", length = 100)
    private String engineName;

    @Column(name = "DISPLACEMENT_CC")
    private Integer displacementCc;

    @Column(name = "TRANSMISSION", length = 50)
    private String transmission;

    @Column(name = "DRIVE_TYPE", length = 100)
    private String driveType;

    @Column(name = "FUEL_EFFICIENCY", length = 20)
    private String fuelEfficiency;

    @Column(name = "CO2_EMISSION")
    private Integer co2Emission;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<CarTrim> trims = new ArrayList<>();
}
