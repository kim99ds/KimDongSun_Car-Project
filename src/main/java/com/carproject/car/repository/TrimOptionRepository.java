package com.carproject.car.repository;

import com.carproject.car.entity.TrimOption;
import com.carproject.global.common.entity.Yn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrimOptionRepository extends JpaRepository<TrimOption, Long> {

    @Query("""
        select t.optionItem.optionItemId
        from TrimOption t
        where t.trim.trimId = :trimId
    """)
    List<Long> findAllowedOptionIdsByTrimId(@Param("trimId") Long trimId);

    @Query("""
        select t.optionItem.optionItemId
        from TrimOption t
        where t.trim.trimId = :trimId
          and t.isRequired = :isRequired
    """)
    List<Long> findRequiredOptionIdsByTrimId(
            @Param("trimId") Long trimId,
            @Param("isRequired") Yn isRequired
    );
}
