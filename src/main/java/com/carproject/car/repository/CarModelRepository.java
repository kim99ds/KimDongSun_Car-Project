package com.carproject.car.repository;

import com.carproject.car.entity.CarModel;
import com.carproject.event.dto.EventCarItemDto;
import com.carproject.main.dto.BestCarCardDto;
import com.carproject.main.dto.BrandPopularCarDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    /**
     * 맞춤견적 추천용: 전체 모델을 brand + variants까지 한 번에 로드
     * - trims까지 fetch하면 컬렉션이 늘어나 성능/예외 위험이 커져서 variants까지만 fetch
     */
    @Query("""
        select distinct m from CarModel m
        join fetch m.brand
        left join fetch m.variants v
    """)
    List<CarModel> findAllWithBrandAndVariants();

    // 1) 차량 필터링 조회 (기존 로직 유지)
    @Query("""
        SELECT DISTINCT m
        FROM CarModel m
        JOIN m.brand b
        JOIN m.variants v
        WHERE
          (
            :q IS NULL
            OR lower(m.modelName) like concat('%', lower(:q), '%')
            OR lower(b.brandName) like concat('%', lower(:q), '%')
          )
        AND
          (:segment IS NULL OR m.segment = :segment)
        AND
          (:brand IS NULL OR b.brandName = :brand)
        AND
          (:engineType IS NULL OR v.engineType = :engineType)
        ORDER BY
          CASE WHEN :sort = 'VIEW' THEN m.viewCount END DESC,
          CASE WHEN :sort = 'LIKE' THEN m.likeCount END DESC,
          m.modelId DESC
    """)
    List<CarModel> searchCars(
            @Param("q") String q,
            @Param("segment") String segment,
            @Param("brand") String brand,
            @Param("engineType") String engineType,
            @Param("sort") String sort
    );

    // ✅ 자동교정 후보 수집용
    @Query("""
        select distinct m.modelName
        from CarModel m
        where m.modelName is not null
    """)
    List<String> findDistinctModelNames();

    // 2) 조회수 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE CarModel m
           SET m.viewCount = m.viewCount + 1
         WHERE m.modelId = :modelId
    """)
    int incrementViewCount(@Param("modelId") Long modelId);

    // 3) 상세 조회
    @Query("""
        select distinct m from CarModel m
        join fetch m.brand
        left join fetch m.variants v
        where m.modelId = :modelId
    """)
    Optional<CarModel> findByIdWithVariants(@Param("modelId") Long modelId);

    // 4) 이벤트 타겟 조건 매칭
    @Query("""
        select distinct new com.carproject.event.dto.EventCarItemDto(
            m.modelId,
            b.brandName,
            m.modelName,
            m.modelYear,
            m.segment
        )
        from CarModel m
        join m.brand b
        left join m.variants v
        where (:brandNames is null or b.brandName in :brandNames)
          and (:segments is null or m.segment in :segments)
          and (:engineTypes is null or v.engineType in :engineTypes)
        order by b.brandName asc, m.modelName asc
    """)
    List<EventCarItemDto> findEventMatchedCars(
            @Param("brandNames") List<String> brandNames,
            @Param("segments") List<String> segments,
            @Param("engineTypes") List<String> engineTypes
    );

    // 5) 이벤트 추천 fallback
    @Query("""
        select new com.carproject.event.dto.EventCarItemDto(
            m.modelId,
            b.brandName,
            m.modelName,
            m.modelYear,
            m.segment
        )
        from CarModel m
        join m.brand b
        order by m.modelYear desc, m.viewCount desc, m.modelId desc
    """)
    List<EventCarItemDto> findRecommendedEventCars();

    // Upsell Step1: 브랜드별 모델 옵션
    interface ModelOptionView {
        Long getModelId();
        String getModelName();
    }

    @Query(value = """
        SELECT
            m.MODEL_ID   AS modelId,
            m.MODEL_NAME AS modelName
        FROM CAR_MODEL m
        JOIN BRAND b ON b.BRAND_ID = m.BRAND_ID
        WHERE b.BRAND_NAME = :brandName
        ORDER BY m.MODEL_NAME ASC
    """, nativeQuery = true)
    List<ModelOptionView> findModelOptionsByBrandName(@Param("brandName") String brandName);

    // Upsell Step1: SEGMENT 목록
    @Query(value = """
        SELECT DISTINCT m.SEGMENT
        FROM CAR_MODEL m
        WHERE m.SEGMENT IS NOT NULL
        ORDER BY m.SEGMENT ASC
    """, nativeQuery = true)
    List<String> findDistinctSegments();

    // ============================================================
    // ✅ 메인 BEST CAR TOP6 (LIKE_COUNT 기준) + 대표이미지(view_type = exterior)
    // - exterior 없으면(데이터 누락) 아무 이미지 1장으로 fallback
    // ============================================================
    @Query("""
        select new com.carproject.main.dto.BestCarCardDto(
            m.modelId,
            b.brandName,
            m.modelName,
            (
              select ci.imageUrl
              from CarImage ci
              where ci.model.modelId = m.modelId
                and ci.imageId = coalesce(
                    (
                      select min(ci2.imageId)
                      from CarImage ci2
                      where ci2.model.modelId = m.modelId
                        and lower(ci2.viewType) = 'exterior'
                    ),
                    (
                      select min(ci3.imageId)
                      from CarImage ci3
                      where ci3.model.modelId = m.modelId
                    )
                )
            ),
            m.likeCount
        )
        from CarModel m
        join m.brand b
        order by m.likeCount desc, m.viewCount desc, m.modelId desc
    """)
    List<BestCarCardDto> findBestTop6(Pageable pageable);

    // ============================================================
    // ✅ 메인 "브랜드 별 인기": 브랜드 1개 기준 좋아요 TOP N (exterior 대표 이미지)
    // ============================================================
    @Query("""
        select new com.carproject.main.dto.BrandPopularCarDto(
            m.modelId,
            m.modelName,
            (
              select ci.imageUrl
              from CarImage ci
              where ci.model.modelId = m.modelId
                and lower(coalesce(ci.viewType, '')) = 'exterior'
                and ci.imageId = (
                    select min(ci2.imageId)
                    from CarImage ci2
                    where ci2.model.modelId = m.modelId
                      and lower(coalesce(ci2.viewType, '')) = 'exterior'
                )
            ),
            m.likeCount
        )
        from CarModel m
        where m.brand.brandId = :brandId
        order by m.likeCount desc, m.viewCount desc, m.modelId desc
    """)
    List<BrandPopularCarDto> findTopLikedByBrand(@Param("brandId") Long brandId, Pageable pageable);

}