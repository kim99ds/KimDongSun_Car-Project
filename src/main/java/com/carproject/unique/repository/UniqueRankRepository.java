package com.carproject.unique.repository;

import com.carproject.unique.dto.TierTrimView;
import com.carproject.unique.dto.TrimSearchItemView;
import com.carproject.car.entity.CarTrim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UniqueRankRepository extends JpaRepository<CarTrim, Long> {

    @Query(value = """
    SELECT
        t.trim_id    AS trimId,
        m.model_id   AS modelId,
        b.brand_name AS brandName,
        m.model_name AS modelName,
        m.model_year AS modelYear,
        t.trim_name  AS trimName,
        t.base_price AS basePrice,
        (
          SELECT MIN(ci.image_url)
          FROM car_image ci
          WHERE ci.model_id = m.model_id
            AND ci.view_type = 'exterior'
        ) AS imageUrl
    FROM car_trim t
    JOIN car_variant v ON t.variant_id = v.variant_id
    JOIN car_model   m ON v.model_id   = m.model_id
    JOIN brand       b ON m.brand_id   = b.brand_id
    WHERE (:q IS NULL OR
           UPPER(b.brand_name) LIKE '%' || UPPER(:q) || '%' OR
           UPPER(m.model_name) LIKE '%' || UPPER(:q) || '%' OR
           UPPER(t.trim_name)  LIKE '%' || UPPER(:q) || '%')
    ORDER BY t.base_price ASC NULLS LAST
    """, nativeQuery = true)
    List<TrimSearchItemView> searchTrims(@Param("q") String q);

    @Query(value = """
    SELECT
        t.trim_id    AS trimId,
        m.model_id   AS modelId,
        b.brand_name AS brandName,
        m.model_name AS modelName,
        m.model_year AS modelYear,
        t.trim_name  AS trimName,
        t.base_price AS basePrice,
        (
          SELECT MIN(ci.image_url)
          FROM car_image ci
          WHERE ci.model_id = m.model_id
            AND ci.view_type = 'exterior'
        ) AS imageUrl
    FROM car_trim t
    JOIN car_variant v ON t.variant_id = v.variant_id
    JOIN car_model   m ON v.model_id   = m.model_id
    JOIN brand       b ON m.brand_id   = b.brand_id
    WHERE t.trim_id = :trimId
    """, nativeQuery = true)
    Optional<TrimSearchItemView> findTrim(@Param("trimId") Long trimId);

    @Query(value = """
    SELECT
        t.trim_id     AS trimId,
        m.model_id    AS modelId,
        m.model_name  AS modelName,
        t.base_price  AS basePrice,
        (
          SELECT MIN(ci.image_url)
          FROM car_image ci
          WHERE ci.model_id = m.model_id
            AND ci.view_type = 'exterior'
        ) AS imageUrl
    FROM car_trim t
    JOIN car_variant v ON v.variant_id = t.variant_id
    JOIN car_model  m ON m.model_id = v.model_id
    WHERE t.base_price >= :minPrice
      AND t.base_price <  :maxPrice
    """, nativeQuery = true)
    List<TierTrimView> findTierTrimsByPriceRange(@Param("minPrice") long minPrice,
                                                 @Param("maxPrice") long maxPrice);
}
