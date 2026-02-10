package com.carproject.car.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CAR_IMAGE", schema = "CAR_PROJECT")
public class CarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAR_IMAGE_GEN")
    @SequenceGenerator(name = "SEQ_CAR_IMAGE_GEN", sequenceName = "CAR_PROJECT.SEQ_CAR_IMAGE", allocationSize = 1)
    @Column(name = "IMAGE_ID")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MODEL_ID", nullable = false)
    private CarModel model;


    @Column(name = "IMAGE_URL", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "VIEW_TYPE", length = 30)
    private String viewType;
}
