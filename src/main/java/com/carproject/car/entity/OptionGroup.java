package com.carproject.car.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "OPTION_GROUP", schema = "CAR_PROJECT")
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPTION_GROUP_GEN")
    @SequenceGenerator(name = "SEQ_OPTION_GROUP_GEN", sequenceName = "CAR_PROJECT.SEQ_OPTION_GROUP", allocationSize = 1)
    @Column(name = "OPTION_GROUP_ID")
    private Long optionGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRAND_ID")
    private Brand brand;

    @Column(name = "GROUP_NAME", nullable = false, length = 200)
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(name = "SELECT_RULE", nullable = false, length = 10)
    private SelectRule selectRule;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @OneToMany(mappedBy = "optionGroup", fetch = FetchType.LAZY)
    private List<OptionGroupItem> groupItems = new ArrayList<>();
}
