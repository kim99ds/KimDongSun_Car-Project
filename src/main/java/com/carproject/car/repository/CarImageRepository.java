package com.carproject.car.repository;

import com.carproject.car.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarImageRepository extends JpaRepository<CarImage, Long> {

    // ForMeRecommendationService (model 기준 1장)
    Optional<CarImage> findFirstByModel_ModelIdOrderByImageIdAsc(
            Long modelId
    );

    // ✅ 모델 기준 + viewType (컬러 무시) : 견적서/기타 공용
    Optional<CarImage> findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(
            Long modelId,
            String viewType
    );
}
