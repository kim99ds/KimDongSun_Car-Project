package com.carproject.car.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "OPTION_DEPENDENCY",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_OPTION_DEP",
        columnNames = {"OPTION_ITEM_ID", "RELATED_OPTION_ITEM_ID", "RULE_TYPE"}
    )
)
public class OptionDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPTION_DEPENDENCY_GEN")
    @SequenceGenerator(
        name = "SEQ_OPTION_DEPENDENCY_GEN",
        sequenceName = "CAR_PROJECT.SEQ_OPTION_DEPENDENCY",
        allocationSize = 1
    )
    @Column(name = "OPTION_DEPENDENCY_ID")
    private Long optionDependencyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OPTION_ITEM_ID", nullable = false)
    private OptionItem optionItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RELATED_OPTION_ITEM_ID", nullable = false)
    private OptionItem relatedOptionItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "RULE_TYPE", nullable = false, length = 10)
    private DependencyRuleType ruleType;
}
