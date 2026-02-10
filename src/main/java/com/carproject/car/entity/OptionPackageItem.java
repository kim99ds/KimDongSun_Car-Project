package com.carproject.car.entity;

import com.carproject.global.common.entity.Yn;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "OPTION_PACKAGE_ITEM",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_OPI_PACKAGE_CHILD", columnNames = {"PACKAGE_OPTION_ITEM_ID", "CHILD_OPTION_ITEM_ID"})
)
public class OptionPackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPTION_PACKAGE_ITEM_GEN")
    @SequenceGenerator(
        name = "SEQ_OPTION_PACKAGE_ITEM_GEN",
        sequenceName = "CAR_PROJECT.SEQ_OPTION_PACKAGE_ITEM",
        allocationSize = 1
    )
    @Column(name = "OPTION_PACKAGE_ITEM_ID")
    private Long optionPackageItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PACKAGE_OPTION_ITEM_ID", nullable = false)
    private OptionItem packageOptionItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CHILD_OPTION_ITEM_ID", nullable = false)
    private OptionItem childOptionItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_INCLUDED", nullable = false, length = 1)
    private Yn isIncluded = Yn.Y;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}
