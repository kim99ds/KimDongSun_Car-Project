package com.carproject.car.entity;

import com.carproject.car.entity.CarTrim;
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
    name = "TRIM_OPTION",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_TRIM_OPTION", columnNames = {"TRIM_ID", "OPTION_ITEM_ID"})
)
public class TrimOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TRIM_OPTION_GEN")
    @SequenceGenerator(name = "SEQ_TRIM_OPTION_GEN", sequenceName = "CAR_PROJECT.SEQ_TRIM_OPTION", allocationSize = 1)
    @Column(name = "TRIM_OPTION_ID")
    private Long trimOptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRIM_ID", nullable = false)
    private CarTrim trim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OPTION_ITEM_ID", nullable = false)
    private OptionItem optionItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_REQUIRED", nullable = false, length = 1)
    private Yn isRequired = Yn.N;
}
