package com.carproject.car.repository;

import com.carproject.car.entity.OptionPackageItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OptionPackageItemRepository extends JpaRepository<OptionPackageItem, Long> {

    /**
     * ✅ (TrimService) 특정 옵션이 "어떤 패키지에 child로 포함되는지" 조회
     */
    List<OptionPackageItem> findByChildOptionItem_OptionItemId(Long childOptionItemId);

    /**
     * ✅ (TrimService) 특정 패키지 옵션이 포함하는 child 매핑들 조회
     */
    List<OptionPackageItem> findByPackageOptionItem_OptionItemId(Long packageOptionItemId);

    /**
     * ✅ (QuoteService) 패키지 옵션에 포함된 child 옵션 조회
     * - IS_INCLUDED = 'Y' 인 것만
     */
    @Query("""
        select opi
          from OptionPackageItem opi
         where opi.packageOptionItem.optionItemId in :packageOptionIds
           and opi.isIncluded = 'Y'
    """)
    List<OptionPackageItem> findIncludedChildrenByPackageIds(
            @Param("packageOptionIds") List<Long> packageOptionIds
    );
}
