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
    name = "OPTION_GROUP_ITEM",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_OGI_GROUP_OPTION", columnNames = {"OPTION_GROUP_ID", "OPTION_ITEM_ID"})
)
public class OptionGroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPTION_GROUP_ITEM_GEN")
    @SequenceGenerator(
        name = "SEQ_OPTION_GROUP_ITEM_GEN",
        sequenceName = "CAR_PROJECT.SEQ_OPTION_GROUP_ITEM",
        allocationSize = 1
    )
    @Column(name = "OPTION_GROUP_ITEM_ID")
    private Long optionGroupItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OPTION_GROUP_ID", nullable = false)
    private OptionGroup optionGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OPTION_ITEM_ID", nullable = false)
    private OptionItem optionItem;
}
