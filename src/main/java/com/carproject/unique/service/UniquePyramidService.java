package com.carproject.unique.service;

import com.carproject.unique.dto.*;
import com.carproject.unique.repository.UniqueRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniquePyramidService {

    private final UniqueRankRepository uniqueRankRepository;

    public PyramidViewDto buildPage(String q, Long trimId, Integer tier, Long focusTrimId) {
        String query = normalize(q);

        // 1) 검색 결과(최대 30개)
        List<TrimSearchItemDto> results = uniqueRankRepository.searchTrims(query).stream()
                .map(this::toDto)
                .limit(30)
                .toList();

        // 2) 검색어 없으면: 초기 화면
        if (query == null) {
            return new PyramidViewDto(null, null, null, null, null, null, null, results, null, null, List.of());
        }

        // 3) trimId 없으면: 대표 1개 자동 선택
        Long selectedTrimId = trimId;
        TrimSearchItemDto selected = null;

        if (selectedTrimId == null) {
            if (results.isEmpty()) {
                return new PyramidViewDto(query, null, null, null, null, null, null, results, null, null, List.of());
            }
            selected = pickBest(results, query);
            selectedTrimId = selected.trimId();
        }

        // 4) 선택 트림 조회
        if (selected == null) {
            TrimSearchItemView selectedView = uniqueRankRepository.findTrim(selectedTrimId).orElse(null);
            if (selectedView != null) selected = toDto(selectedView);
        }
        if (selected == null) {
            return new PyramidViewDto(query, null, null, null, null, null, null, results, null, null, List.of());
        }

        Long selectedModelId = selected.modelId();
        Integer selectedTier = toTierByPrice(selected.basePrice());
        String selectedImageUrl = selected.imageUrl();

        // 5) tier 파라미터 없으면: 피라미드 화면
        if (tier == null) {
            return new PyramidViewDto(
                    query,
                    selectedTrimId,
                    selectedModelId,
                    selected.modelName(),
                    selectedImageUrl,
                    selected.basePrice(),
                    selectedTier,
                    results,
                    null,
                    null,
                    List.of()
            );
        }

        // 6) tier 화면
        PriceRange range = priceRangeForTier(tier);

        List<TierTrimView> tierCarsAll = uniqueRankRepository.findTierTrimsByPriceRange(range.min(), range.max());

        // 모델별 최저가 trim 1개
        Map<Long, TierTrimView> cheapestByModel = new HashMap<>();
        for (TierTrimView t : tierCarsAll) {
            Long modelId = t.getModelId();
            if (modelId == null) continue;

            long price = (t.getBasePrice() == null) ? Long.MAX_VALUE : t.getBasePrice();
            TierTrimView cur = cheapestByModel.get(modelId);
            long curPrice = (cur == null || cur.getBasePrice() == null) ? Long.MAX_VALUE : cur.getBasePrice();

            if (cur == null || price < curPrice) {
                cheapestByModel.put(modelId, t);
            }
        }

        // 최대 9개(가격순)
        List<TierCarCardDto> tierCarsCheapest = cheapestByModel.values().stream()
                .sorted(Comparator.comparingLong(v -> v.getBasePrice() == null ? Long.MAX_VALUE : v.getBasePrice()))
                .limit(9)
                .map(v -> new TierCarCardDto(v.getTrimId(), v.getModelId(), v.getModelName(), v.getBasePrice(), v.getImageUrl()))
                .toList();

        TierCarCardDto mainCard;

        if (focusTrimId != null) {
            TierTrimView focus = tierCarsAll.stream()
                    .filter(v -> Objects.equals(v.getTrimId(), focusTrimId))
                    .findFirst()
                    .orElse(null);

            if (focus != null) {
                mainCard = new TierCarCardDto(focus.getTrimId(), focus.getModelId(), focus.getModelName(), focus.getBasePrice(), focus.getImageUrl());
            } else {
                TrimSearchItemView focusView = uniqueRankRepository.findTrim(focusTrimId).orElse(null);
                if (focusView != null) {
                    TrimSearchItemDto f = toDto(focusView);
                    mainCard = new TierCarCardDto(f.trimId(), f.modelId(), f.modelName(), f.basePrice(), f.imageUrl());
                } else {
                    mainCard = null;
                }
            }
        } else {
            if (Objects.equals(tier, selectedTier)) {
                mainCard = new TierCarCardDto(selected.trimId(), selected.modelId(), selected.modelName(), selected.basePrice(), selected.imageUrl());
            } else {
                mainCard = tierCarsCheapest.isEmpty() ? null : tierCarsCheapest.get(0);
            }
        }

        final Long mainTrimId = (mainCard == null) ? null : mainCard.trimId();

        List<TierCarCardDto> grid = new ArrayList<>();
        for (TierCarCardDto c : tierCarsCheapest) {
            if (mainTrimId != null && Objects.equals(c.trimId(), mainTrimId)) continue;
            grid.add(c);
            if (grid.size() == 9) break;
        }

        if (grid.size() < 9 && mainTrimId != null
                && grid.stream().noneMatch(x -> Objects.equals(x.trimId(), mainTrimId))) {
            grid.add(mainCard);
        }

        return new PyramidViewDto(
                query,
                selectedTrimId,
                selectedModelId,
                selected.modelName(),
                selectedImageUrl,
                selected.basePrice(),
                selectedTier,
                results,
                tier,
                mainCard,
                grid
        );
    }

    private TrimSearchItemDto pickBest(List<TrimSearchItemDto> results, String query) {
        String q = query.toLowerCase();
        TrimSearchItemDto best = null;

        for (TrimSearchItemDto r : results) {
            boolean match = r.displayName() != null && r.displayName().toLowerCase().contains(q);

            if (best == null) { best = r; continue; }

            boolean bestMatch = best.displayName() != null && best.displayName().toLowerCase().contains(q);

            if (match && !bestMatch) { best = r; continue; }

            long bp = best.basePrice() == null ? Long.MAX_VALUE : best.basePrice();
            long rp = r.basePrice() == null ? Long.MAX_VALUE : r.basePrice();

            if (match == bestMatch && rp < bp) best = r;
        }
        return best;
    }

    private TrimSearchItemDto toDto(TrimSearchItemView v) {
        return new TrimSearchItemDto(
                v.getTrimId(),
                v.getModelId(),
                v.getBrandName(),
                v.getModelName(),
                v.getModelYear(),
                v.getTrimName(),
                v.getBasePrice(),
                v.getImageUrl()
        );
    }

    private Integer toTierByPrice(Long basePrice) {
        long p = (basePrice == null) ? 0L : basePrice;

        if (p >= 100_000_000L) return 1;
        if (p >= 70_000_000L)  return 2;
        if (p >= 50_000_000L)  return 3;
        if (p >= 30_000_000L)  return 4;
        return 5;
    }

    private String normalize(String q) {
        if (q == null) return null;
        String s = q.trim();
        return s.isBlank() ? null : s;
    }

    private record PriceRange(long min, long max) {}

    private PriceRange priceRangeForTier(int tier) {
        return switch (tier) {
            case 1 -> new PriceRange(100_000_000L, Long.MAX_VALUE);
            case 2 -> new PriceRange(70_000_000L, 100_000_000L);
            case 3 -> new PriceRange(50_000_000L, 70_000_000L);
            case 4 -> new PriceRange(30_000_000L, 50_000_000L);
            default -> new PriceRange(0L, 30_000_000L);
        };
    }
}
