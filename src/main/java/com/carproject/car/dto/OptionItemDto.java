package com.carproject.car.dto;

import com.carproject.car.entity.OptionCategory;
import com.carproject.car.entity.OptionType;
import com.carproject.car.entity.SelectRule;
import com.carproject.global.common.entity.Yn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter // ⭐ setter 필수
@NoArgsConstructor
@AllArgsConstructor
public class OptionItemDto {

    private Long optionId;
    private String optionName;
    private BigDecimal optionPrice;
    private Yn required;

    private OptionType optionType;
    private OptionCategory optionCategory;
    private SelectRule selectRule;
    private String optionDesc;

    private Long groupId;
    private String groupName;
    private SelectRule groupRule;

    // SINGLE → 포함된 PACKAGE
    private List<IncludedPackageDto> includedPackages;

    // PACKAGE → 포함된 SINGLE
    private List<IncludedOptionDto> includedOptions;

    /**
     * 이 옵션을 선택하려면 함께 선택되어 있어야 하는 옵션 목록(= REQUIRES)
     * - 프론트에서 선행조건 미충족 시 선택 불가(disabled) 처리용
     */
    private List<Long> requiresOptionIds;

    /**
     * 이 옵션을 선택할 수 없게 만드는 옵션 목록(= EXCLUDES)
     * - 프론트에서 충돌 옵션 선택 시 선택 불가(disabled) 처리용
     */
    private List<Long> excludesOptionIds;
}
