package com.carproject.car.repository;

import com.carproject.car.entity.DependencyRuleType;
import com.carproject.car.entity.OptionDependency;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OptionDependencyRepository extends JpaRepository<OptionDependency, Long> {

    /**
     * REQUIRES 간선 조회용 Projection
     * - OPTION_DEPENDENCY는 OptionItem을 FK로 들고 있으므로 optionItem.optionItemId 형태로 접근해야 한다.
     */
    interface RequiresEdge {
        Long getOptionItemId();
        Long getRelatedOptionItemId();
    }

    /**
     * EXCLUDES 간선 조회용 Projection
     */
    interface ExcludesEdge {
        Long getOptionItemId();
        Long getRelatedOptionItemId();
    }

    List<OptionDependency> findByRuleTypeAndOptionItem_OptionItemIdIn(
            DependencyRuleType ruleType,
            Collection<Long> optionItemIds
    );

    /**
     * 선택된 option_item_id들에 대해 REQUIRES(선행필수) 관계를 조회한다.
     */
    @Query("""
        select d.optionItem.optionItemId as optionItemId,
               d.relatedOptionItem.optionItemId as relatedOptionItemId
          from OptionDependency d
         where d.ruleType = com.carproject.car.entity.DependencyRuleType.REQUIRES
           and d.optionItem.optionItemId in :optionItemIds
    """)
    List<RequiresEdge> findRequiresEdgesByOptionItemIds(@Param("optionItemIds") List<Long> optionItemIds);

    /**
     * 선택된 option_item_id들에 대해 EXCLUDES(동시선택불가) 관계를 조회한다.
     */
    @Query("""
        select d.optionItem.optionItemId as optionItemId,
               d.relatedOptionItem.optionItemId as relatedOptionItemId
          from OptionDependency d
         where d.ruleType = com.carproject.car.entity.DependencyRuleType.EXCLUDES
           and d.optionItem.optionItemId in :optionItemIds
    """)
    List<ExcludesEdge> findExcludesEdgesByOptionItemIds(@Param("optionItemIds") List<Long> optionItemIds);
}
