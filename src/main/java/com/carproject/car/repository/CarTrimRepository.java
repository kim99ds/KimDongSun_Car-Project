package com.carproject.car.repository;

import com.carproject.car.entity.CarTrim;
import com.carproject.unique.dto.TrimPickView;
import com.carproject.unique.dto.UpsellCandidateView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarTrimRepository extends JpaRepository<CarTrim, Long> {

    /* =========================================================
       1) 상세 로딩 (기존 유지)
    ========================================================= */

    @Query("""
        select distinct t from CarTrim t
        left join fetch t.trimColors tc
        left join fetch tc.color
        left join fetch t.trimOptions to
        left join fetch to.optionItem oi
        left join fetch oi.optionGroupItems ogi
        left join fetch ogi.optionGroup
        where t.trimId = :trimId
    """)
    Optional<CarTrim> findDetailById(@Param("trimId") Long trimId);

    @Query(value = """
        SELECT
            m.MODEL_ID AS modelId,
            MIN(t.BASE_PRICE) AS minBasePrice
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        GROUP BY m.MODEL_ID
    """, nativeQuery = true)
    List<com.carproject.car.repository.ModelMinPriceView> findModelMinPrices();

    @Query(value = """
        SELECT DISTINCT m.MODEL_ID AS modelId
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        JOIN TRIM_OPTION topt ON topt.TRIM_ID = t.TRIM_ID
        JOIN OPTION_ITEM oi ON oi.OPTION_ITEM_ID = topt.OPTION_ITEM_ID
        WHERE oi.OPTION_NAME LIKE '%6인승%'
           OR oi.OPTION_NAME LIKE '%7인승%'
    """, nativeQuery = true)
    List<com.carproject.car.repository.ModelIdView> findSeatCapableModelIds();

    @Query(value = """
        SELECT
            m.MODEL_ID AS modelId,
            COUNT(DISTINCT oi.OPTION_ITEM_ID) AS cnt
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        JOIN TRIM_OPTION topt ON topt.TRIM_ID = t.TRIM_ID
        JOIN OPTION_ITEM oi ON oi.OPTION_ITEM_ID = topt.OPTION_ITEM_ID
        WHERE UPPER(oi.OPTION_CATEGORY) = 'ASSISTANT'
        GROUP BY m.MODEL_ID
    """, nativeQuery = true)
    List<com.carproject.car.repository.ModelCountView> findAssistantOptionCountsByModel();

    /* =========================================================
       4) Upsell v2 Step1 카드(기존 유지)
    ========================================================= */

    interface UpsellCardView {
        Long getTrimId();
        String getBrandName();
        String getModelName();
        String getTrimName();
        Long getBasePrice();
    }

    @Query(value = """
        SELECT
            t.TRIM_ID    AS trimId,
            b.BRAND_NAME AS brandName,
            m.MODEL_NAME AS modelName,
            t.TRIM_NAME  AS trimName,
            t.BASE_PRICE AS basePrice
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
        WHERE m.MODEL_ID = :modelId
        ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC
    """, nativeQuery = true)
    List<UpsellCardView> findUpsellCardsByModelId(@Param("modelId") Long modelId);

    /* =========================================================
       5) Step2/Compare에서 공통으로 쓰는 picked 정보
    ========================================================= */

    interface PickedTrimDetailView {
        Long getTrimId();
        Long getModelId();
        String getBrandName();
        String getModelName();
        String getTrimName();
        Long getBasePrice();
        String getSegment();
    }

    @Query(value = """
        SELECT
            t.TRIM_ID    AS trimId,
            m.MODEL_ID   AS modelId,
            b.BRAND_NAME AS brandName,
            m.MODEL_NAME AS modelName,
            t.TRIM_NAME  AS trimName,
            t.BASE_PRICE AS basePrice,
            m.SEGMENT    AS segment
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
        WHERE t.TRIM_ID = :trimId
    """, nativeQuery = true)
    PickedTrimDetailView findPickedTrimDetail(@Param("trimId") Long trimId);

    /* =========================================================
       Step2 후보 뷰
    ========================================================= */

    interface UpsellModelMinTrimView {
        Long getTrimId();
        Long getModelId();
        String getBrandName();
        String getModelName();
        String getTrimName();
        Long getBasePrice();
        String getSegment();
    }

    /* =========================================================
       ✅ [추가] 6-A) InRange + "모델명키"까지 제외
    ========================================================= */

    @Query(value = """
        SELECT *
        FROM (
            SELECT
                MIN(t.TRIM_ID) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimId,
                MIN(m.MODEL_ID) AS modelId,
                MIN(b.BRAND_NAME) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS brandName,
                MIN(m.MODEL_NAME) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS modelName,
                MIN(t.TRIM_NAME)  KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimName,
                MIN(t.BASE_PRICE) AS basePrice,
                MIN(m.SEGMENT) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS segment,
                MIN(UPPER(TRIM(m.MODEL_NAME))) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS modelNameKey
            FROM CAR_TRIM t
            JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
            JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
            JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
            WHERE t.BASE_PRICE > :pickedBasePrice
              AND t.BASE_PRICE <= :upperPrice
              AND m.MODEL_ID <> :excludeModelId
              AND m.SEGMENT = :pickedSegment
              AND UPPER(TRIM(m.MODEL_NAME)) <> :excludeModelNameKey
            GROUP BY m.MODEL_ID
            ORDER BY
              CASE
                WHEN UPPER(MIN(b.COUNTRY_CODE) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC)) = 'KOR' THEN 1
                ELSE 0
              END ASC,
              basePrice ASC,
              trimId ASC
        )
        WHERE ROWNUM <= :limit
    """, nativeQuery = true)
    List<UpsellModelMinTrimView> findUpsellModelMinTrimsInRangeExcludeModelNameKey(
            @Param("pickedBasePrice") Long pickedBasePrice,
            @Param("upperPrice") Long upperPrice,
            @Param("pickedSegment") String pickedSegment,
            @Param("excludeModelId") Long excludeModelId,
            @Param("excludeModelNameKey") String excludeModelNameKey,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM (
            SELECT UPPER(TRIM(m.MODEL_NAME)) AS modelKey
            FROM CAR_TRIM t
            JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
            JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
            JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
            WHERE (:brandCount = 0 OR b.BRAND_NAME IN (:brands))
              AND (:segmentCount = 0 OR m.SEGMENT IN (:segments))
              AND (
                    :keyword IS NULL OR :keyword = '' OR
                    UPPER(m.MODEL_NAME) LIKE '%' || UPPER(:keyword) || '%'
                 OR UPPER(t.TRIM_NAME)  LIKE '%' || UPPER(:keyword) || '%'
              )
            GROUP BY UPPER(TRIM(m.MODEL_NAME))
        )
    """, nativeQuery = true)
    long countUpsellCardsFiltered(
            @Param("brandCount") int brandCount,
            @Param("brands") List<String> brands,
            @Param("segmentCount") int segmentCount,
            @Param("segments") List<String> segments,
            @Param("keyword") String keyword
    );

    @Query(value = """
        SELECT *
        FROM (
            SELECT
                base.trimId    AS trimId,
                base.brandName AS brandName,
                base.modelName AS modelName,
                base.trimName  AS trimName,
                base.basePrice AS basePrice,
                ROW_NUMBER() OVER (ORDER BY base.basePrice ASC, base.trimId ASC) AS rn
            FROM (
                SELECT
                    MIN(t.TRIM_ID) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimId,
                    MIN(b.BRAND_NAME) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS brandName,
                    MIN(m.MODEL_NAME) KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS modelName,
                    MIN(t.TRIM_NAME)  KEEP (DENSE_RANK FIRST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimName,
                    MIN(t.BASE_PRICE) AS basePrice
                FROM CAR_TRIM t
                JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
                JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
                JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
                WHERE (:brandCount = 0 OR b.BRAND_NAME IN (:brands))
                  AND (:segmentCount = 0 OR m.SEGMENT IN (:segments))
                  AND (
                        :keyword IS NULL OR :keyword = '' OR
                        UPPER(m.MODEL_NAME) LIKE '%' || UPPER(:keyword) || '%'
                     OR UPPER(t.TRIM_NAME)  LIKE '%' || UPPER(:keyword) || '%'
                  )
                GROUP BY UPPER(TRIM(m.MODEL_NAME))
            ) base
        )
        WHERE rn BETWEEN :start AND :end
    """, nativeQuery = true)
    List<UpsellCardView> findUpsellCardsFilteredPaged(
            @Param("brandCount") int brandCount,
            @Param("brands") List<String> brands,
            @Param("segmentCount") int segmentCount,
            @Param("segments") List<String> segments,
            @Param("keyword") String keyword,
            @Param("start") int start,
            @Param("end") int end
    );

    /* =========================================================
   ✅ [추가] 제한없음: 저장된 차량 중 "가장 비싼" 차량 5대
   - 모델별 최고가 트림 1개씩 뽑아서 basePrice DESC
   - pickedTrimId는 결과에서 제외(중복 방지)
========================================================= */

    @Query(value = """
    SELECT *
    FROM (
        SELECT
            MAX(t.TRIM_ID) KEEP (DENSE_RANK LAST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimId,
            MIN(m.MODEL_ID) AS modelId,
            MIN(b.BRAND_NAME) KEEP (DENSE_RANK LAST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS brandName,
            MIN(m.MODEL_NAME) KEEP (DENSE_RANK LAST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS modelName,
            MIN(t.TRIM_NAME)  KEEP (DENSE_RANK LAST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS trimName,
            MAX(t.BASE_PRICE) AS basePrice,
            MIN(m.SEGMENT) KEEP (DENSE_RANK LAST ORDER BY t.BASE_PRICE ASC, t.TRIM_ID ASC) AS segment
        FROM CAR_TRIM t
        JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
        JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
        JOIN BRAND      b ON b.BRAND_ID   = m.BRAND_ID
        WHERE t.TRIM_ID <> :excludeTrimId
        GROUP BY m.MODEL_ID
        ORDER BY basePrice DESC, trimId DESC
    )
    WHERE ROWNUM <= :limit
""", nativeQuery = true)
    List<UpsellModelMinTrimView> findTopExpensiveModelTrimsExcludeTrimId(
            @Param("excludeTrimId") Long excludeTrimId,
            @Param("limit") int limit
    );

    /* =========================================================
   ✅ [추가] trimId → modelId 조회 (이미지용)
   - unique/upsell 카드 이미지 조회에 사용
   - 기존 구조 변경 없이 메서드만 추가
========================================================= */

    interface TrimModelIdView {
        Long getModelId();
    }

    @Query(value = """
    SELECT m.MODEL_ID AS modelId
    FROM CAR_TRIM t
    JOIN CAR_VARIANT v ON v.VARIANT_ID = t.VARIANT_ID
    JOIN CAR_MODEL  m ON m.MODEL_ID   = v.MODEL_ID
    WHERE t.TRIM_ID = :trimId
""", nativeQuery = true)
    Optional<TrimModelIdView> findModelIdByTrimId(@Param("trimId") Long trimId);

}