package com.carproject.car.service;

import com.carproject.car.dto.*;
import com.carproject.car.entity.*;
import com.carproject.car.repository.CarTrimRepository;
import com.carproject.car.repository.OptionDependencyRepository;
import com.carproject.car.repository.OptionPackageItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrimService {

    private final CarTrimRepository trimRepository;
    private final OptionPackageItemRepository optionPackageItemRepository;
    private final OptionDependencyRepository optionDependencyRepository;

    @Transactional(readOnly = true)
    public TrimDetailDto getTrimDetail(Long trimId) {

        CarTrim trim = trimRepository.findDetailById(trimId)
                .orElseThrow(() -> new IllegalArgumentException("트림 없음"));

        // ===== REQUIRES(선행필수) 규칙: 프론트에서 선택 불가 처리용 =====
        List<Long> trimOptionIds = trim.getTrimOptions().stream()
                .map(to -> to.getOptionItem().getOptionItemId())
                .toList();

        Map<Long, List<Long>> requiresMap = new HashMap<>();
        Map<Long, List<Long>> excludesMap = new HashMap<>();
        if (!trimOptionIds.isEmpty()) {
            List<OptionDependencyRepository.RequiresEdge> edges =
                    optionDependencyRepository.findRequiresEdgesByOptionItemIds(trimOptionIds);
            for (OptionDependencyRepository.RequiresEdge e : edges) {
                requiresMap.computeIfAbsent(e.getOptionItemId(), k -> new ArrayList<>())
                        .add(e.getRelatedOptionItemId());
            }

            List<OptionDependencyRepository.ExcludesEdge> exEdges =
                    optionDependencyRepository.findExcludesEdgesByOptionItemIds(trimOptionIds);
            for (OptionDependencyRepository.ExcludesEdge e : exEdges) {
                excludesMap.computeIfAbsent(e.getOptionItemId(), k -> new ArrayList<>())
                        .add(e.getRelatedOptionItemId());
            }
        }

        List<OptionItemDto> optionDtos =
                trim.getTrimOptions().stream()
                        .map(trimOption -> {

                            OptionItem oi = trimOption.getOptionItem();

                            // ===== 옵션 그룹 =====
                            OptionGroupItem ogi = oi.getOptionGroupItems().stream()
                                    .min(Comparator.comparing(OptionGroupItem::getOptionGroupItemId))
                                    .orElse(null);

                            Long groupId = ogi != null
                                    ? ogi.getOptionGroup().getOptionGroupId()
                                    : null;

                            String groupName = ogi != null
                                    ? ogi.getOptionGroup().getGroupName()
                                    : null;

                            SelectRule groupRule = ogi != null
                                    ? ogi.getOptionGroup().getSelectRule()
                                    : null;

                            OptionItemDto dto = new OptionItemDto(
                                    oi.getOptionItemId(),
                                    oi.getOptionName(),
                                    oi.getOptionPrice(),
                                    trimOption.getIsRequired(),
                                    oi.getOptionType(),
                                    oi.getOptionCategory(),
                                    oi.getSelectRule(),
                                    oi.getOptionDesc(),
                                    groupId,
                                    groupName,
                                    groupRule,
                                    new ArrayList<>(), // includedPackages
                                    new ArrayList<>(),  // includedOptions
                                    new ArrayList<>(requiresMap.getOrDefault(oi.getOptionItemId(), List.of())),
                                    new ArrayList<>(excludesMap.getOrDefault(oi.getOptionItemId(), List.of()))
                            );

                            /* =========================
                               SINGLE → 포함된 PACKAGE
                               ========================= */
                            if (oi.getOptionType() == OptionType.SINGLE) {
                                dto.setIncludedPackages(
                                        optionPackageItemRepository
                                                .findByChildOptionItem_OptionItemId(oi.getOptionItemId())
                                                .stream()
                                                .map(m -> new IncludedPackageDto(
                                                        m.getPackageOptionItem().getOptionItemId(),
                                                        m.getPackageOptionItem().getOptionName()
                                                ))
                                                .toList()
                                );
                            }

                            /* =========================
                               PACKAGE → 포함된 SINGLE
                               ========================= */
                            if (oi.getOptionType() == OptionType.PACKAGE) {
                                dto.setIncludedOptions(
                                        optionPackageItemRepository
                                                .findByPackageOptionItem_OptionItemId(oi.getOptionItemId())
                                                .stream()
                                                .map(m -> new IncludedOptionDto(
                                                        m.getChildOptionItem().getOptionItemId(),
                                                        m.getChildOptionItem().getOptionName()
                                                ))
                                                .toList()
                                );
                            }

                            return dto;
                        })
                        .toList();

        return new TrimDetailDto(
                trim.getTrimId(),
                trim.getTrimName(),
                trim.getBasePrice(),
                trim.getTrimColors().stream()
                        .map(tc -> new TrimColorDto(
                                tc.getTrimColorId(),
                                tc.getColor().getColorName(),
                                tc.getColor().getColorPrice()
                        ))
                        .toList(),
                optionDtos
        );
    }
}
