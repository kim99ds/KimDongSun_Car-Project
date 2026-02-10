package com.carproject.car.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "OPTION_ITEM", schema = "CAR_PROJECT")
public class OptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPTION_ITEM_GEN")
    @SequenceGenerator(name = "SEQ_OPTION_ITEM_GEN", sequenceName = "CAR_PROJECT.SEQ_OPTION_ITEM", allocationSize = 1)
    @Column(name = "OPTION_ITEM_ID")
    private Long optionItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRAND_ID")
    private Brand brand;

    @Column(name = "OPTION_NAME", nullable = false, length = 1000)
    private String optionName;

    @Column(name = "OPTION_DESC", length = 4000)
    private String optionDesc;

    @Column(name = "OPTION_PRICE", nullable = false)
    private BigDecimal optionPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPTION_TYPE", nullable = false, length = 30)
    private OptionType optionType;

    /**
     * ✅ DB 체크 제약(CK_OPTION_CATEGORY):
     * option_category IN ('wheel','assistant','interior','seats','etc')
     *
     * - 요청/화면에서는 enum(대문자)로 받고
     * - DB에는 소문자 5개만 저장되도록 "OPTION_ITEM 전용 컨버터"를 국소 적용한다.
     */
    @Column(name = "OPTION_CATEGORY", nullable = false, length = 30)
    @Convert(converter = OptionItemOptionCategoryConverter.class)
    private OptionCategory optionCategory = OptionCategory.ETC;

    @Enumerated(EnumType.STRING)
    @Column(name = "SELECT_RULE", nullable = false, length = 10)
    private SelectRule selectRule = SelectRule.MULTI;

    /**
     * DDL에 남아있는 컬럼(OPTION_GROUP / OPTION_GROUP_ITEM과 중복 구조).
     * 현재 서비스 로직에서는 OPTION_GROUP/OPTION_GROUP_ITEM을 정본으로 사용하고,
     * 아래 3개 컬럼은 "레거시/보조" 용도로만 매핑한다.
     */

    @OneToMany(mappedBy = "optionItem", fetch = FetchType.LAZY)
    private List<OptionGroupItem> optionGroupItems = new ArrayList<>();

    @OneToMany(mappedBy = "optionItem", fetch = FetchType.LAZY)
    private List<TrimOption> trimOptions = new ArrayList<>();
}
