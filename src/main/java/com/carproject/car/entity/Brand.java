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
@Table(name = "BRAND", schema = "CAR_PROJECT")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRAND_GEN")
    @SequenceGenerator(name = "SEQ_BRAND_GEN", sequenceName = "CAR_PROJECT.SEQ_BRAND", allocationSize = 1)
    @Column(name = "BRAND_ID")
    private Long brandId;

    @Column(name = "BRAND_NAME", nullable = false, length = 50)
    private String brandName;

    // ✅ 추가: 브랜드 국적 코드 (KOR, JPN, USA, GER ...)
    @Column(name = "COUNTRY_CODE", length = 3)
    private String countryCode;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (countryCode != null) {
            countryCode = countryCode.trim().toUpperCase();
            if (countryCode.isEmpty()) countryCode = null;
        }
        if (brandName != null) {
            brandName = brandName.trim();
        }
    }

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<CarModel> models = new ArrayList<>();
}
