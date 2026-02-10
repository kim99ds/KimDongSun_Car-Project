package com.carproject.car.repository;

import com.carproject.car.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    // ✅ 메인: 랜덤 브랜드 n개 (Oracle)
    @Query(value = """
            SELECT *
              FROM CAR_PROJECT.BRAND
             ORDER BY DBMS_RANDOM.VALUE
             FETCH FIRST :limit ROWS ONLY
            """, nativeQuery = true)
    List<Brand> findRandomBrands(@Param("limit") int limit);

}