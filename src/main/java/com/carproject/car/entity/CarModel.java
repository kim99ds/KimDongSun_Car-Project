package com.carproject.car.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CAR_MODEL", schema = "CAR_PROJECT")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CAR_MODEL_GEN")
    @SequenceGenerator(name = "SEQ_CAR_MODEL_GEN", sequenceName = "CAR_PROJECT.SEQ_CAR_MODEL", allocationSize = 1)
    @Column(name = "MODEL_ID")
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BRAND_ID", nullable = false)
    private Brand brand;

    @Column(name = "MODEL_NAME", nullable = false, length = 100)
    private String modelName;

    @Column(name = "MODEL_YEAR")
    private Integer modelYear;

    @Column(name = "SEGMENT", length = 50)
    private String segment;

    @Column(name = "RELEASE_DATE")
    private LocalDate releaseDate;

    @Column(name = "VIEW_COUNT", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "LIKE_COUNT", nullable = false)
    private Long likeCount = 0L;

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    private List<CarVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    private List<CarImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY)
    private List<ModelLike> modelLikes = new ArrayList<>();
}
