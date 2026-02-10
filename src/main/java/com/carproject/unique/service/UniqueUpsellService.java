package com.carproject.unique.service;

import com.carproject.car.repository.BrandRepository;
import com.carproject.car.repository.CarImageRepository;
import com.carproject.car.repository.CarModelRepository;
import com.carproject.car.repository.CarTrimRepository;
import com.carproject.unique.dto.BrandOptionDto;
import com.carproject.unique.dto.ModelOptionDto;
import com.carproject.unique.dto.UniqueComparePageDto;
import com.carproject.unique.dto.UniqueUpsellCandidatesPageDto;
import com.carproject.unique.dto.UniqueUpsellCarCardDto;
import com.carproject.unique.dto.UniqueUpsellPickPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniqueUpsellService {

    private final BrandRepository brandRepository;
    private final CarModelRepository carModelRepository;
    private final CarTrimRepository carTrimRepository;
    private final CarImageRepository carImageRepository; // ✅ 추가

    private static final String DEFAULT_MAIN_IMG = "/images/santafe1.webp";

    // ✅ 결과 차량 5대만
    private static final int CANDIDATE_LIMIT = 5;

    private static final long ADD_5M  = 5_000_000L;
    private static final long ADD_10M = 10_000_000L;
    private static final long ADD_15M = 15_000_000L;
    private static final long ADD_20M = 20_000_000L;

    // ✅ NEW: 제한없음 값 (UI에서 addPrice=-1 로 넘어오게)
    private static final long ADD_UNLIMITED = -1L;

    /* =========================================================
       Step1 : pick page
       - ✅ 브랜드 다중선택 ORA-00920 해결 (리스트 IS NULL 제거)
       - ✅ 세그먼트 다중선택 지원
       - ✅ 키워드/추가금액/페이징 유지
       - ✅ 카드 이미지: view_type='con' (model_id 기반)
         * UpsellCardView에 modelId가 없으므로 trimId -> modelId 조회로 처리
    ========================================================= */
    public UniqueUpsellPickPageDto buildPickPage(
            List<String> brands,
            List<String> segments,
            Long modelId,
            Long addPrice,
            Integer page,
            String keyword
    ) {
        long finalAddPrice = normalizeAddPrice(addPrice);

        // 선택값 정리(공백 제거)
        List<String> selectedBrands = (brands == null) ? List.of()
                : brands.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .toList();

        List<String> selectedSegments = (segments == null) ? List.of()
                : segments.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .toList();

        // 옵션 목록: 브랜드
        List<BrandOptionDto> brandOptions = brandRepository.findAll().stream()
                .map(b -> new BrandOptionDto(b.getBrandId(), b.getBrandName()))
                .sorted((a, b) -> a.getBrandName().compareToIgnoreCase(b.getBrandName()))
                .toList();

        // 옵션 목록: 세그먼트
        List<String> segmentOptions = carModelRepository.findDistinctSegments().stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .toList();

        String safeKeyword = (keyword == null) ? "" : keyword.trim();

        int pageSize = 9;
        int currentPage = (page == null || page < 1) ? 1 : page;

        // 모델 옵션(브랜드 1개 선택 시만 보여줌)
        List<ModelOptionDto> models = List.of();
        Long selectedModelId = modelId;

        if (selectedBrands.size() == 1) {
            String onlyBrand = selectedBrands.get(0);
            models = carModelRepository.findModelOptionsByBrandName(onlyBrand).stream()
                    .map(v -> new ModelOptionDto(v.getModelId(), v.getModelName()))
                    .toList();
        } else {
            selectedModelId = null; // 브랜드 다중선택이면 modelId는 무시(옵션 UI 의미없음)
        }

        // ✅ 특정 모델을 찍고 들어온 경우: 해당 모델에서 최저가 트림 1개만 카드로 노출
        if (selectedModelId != null) {
            List<CarTrimRepository.UpsellCardView> rows = carTrimRepository.findUpsellCardsByModelId(selectedModelId);

            List<UniqueUpsellCarCardDto> cards = rows.isEmpty()
                    ? List.of()
                    : List.of(new UniqueUpsellCarCardDto(
                    rows.get(0).getTrimId(),
                    null, // pick 카드에서는 modelId 굳이 안 씀(기존 유지)
                    rows.get(0).getBrandName(),
                    rows.get(0).getModelName(),
                    rows.get(0).getTrimName(),
                    rows.get(0).getBasePrice(),
                    resolveConImageOrDefaultByTrimId(rows.get(0).getTrimId()) // ✅ con 이미지
            ));

            return new UniqueUpsellPickPageDto(
                    brandOptions,
                    selectedBrands,
                    segmentOptions,
                    selectedSegments,
                    models,
                    selectedModelId,
                    finalAddPrice,
                    safeKeyword,
                    cards,
                    1,
                    1,
                    cards.size(),
                    pageSize
            );
        }

        // ✅ Oracle(nativeQuery) 안전 처리:
        // - 리스트에 IS NULL 비교하지 말고 count로 on/off
        // - count=0일 때도 IN(:list)가 깨지지 않게 더미 1개 넣기
        int brandCount = selectedBrands.size();
        int segmentCount = selectedSegments.size();

        List<String> brandParam = (brandCount == 0) ? List.of("__DUMMY_BRAND__") : selectedBrands;
        List<String> segmentParam = (segmentCount == 0) ? List.of("__DUMMY_SEG__") : selectedSegments;

        long totalItems = carTrimRepository.countUpsellCardsFiltered(
                brandCount, brandParam,
                segmentCount, segmentParam,
                safeKeyword
        );

        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * pageSize + 1;
        int end = currentPage * pageSize;

        List<CarTrimRepository.UpsellCardView> rows =
                carTrimRepository.findUpsellCardsFilteredPaged(
                        brandCount, brandParam,
                        segmentCount, segmentParam,
                        safeKeyword,
                        start, end
                );

        // ✅ cards 반드시 생성 + 이미지(con)
        List<UniqueUpsellCarCardDto> cards = rows.stream()
                .map(v -> new UniqueUpsellCarCardDto(
                        v.getTrimId(),
                        null, // pick 카드에서는 modelId null 유지
                        v.getBrandName(),
                        v.getModelName(),
                        v.getTrimName(),
                        v.getBasePrice(),
                        resolveConImageOrDefaultByTrimId(v.getTrimId()) // ✅ con 이미지
                ))
                .toList();

        return new UniqueUpsellPickPageDto(
                brandOptions,
                selectedBrands,
                segmentOptions,
                selectedSegments,
                models,
                null,
                finalAddPrice,
                safeKeyword,
                cards,
                currentPage,
                totalPages,
                totalItems,
                pageSize
        );
    }

    /* =========================================================
       Step2 : candidates page
       ✅ "선택한 차량 + addPrice 범위 내" + "같은 세그먼트만"
       ✅ 결과 5개 제한(CANDIDATE_LIMIT)
       ✅ NEW: addPrice=-1(제한없음) 이면 "가장 비싼 차량 5개"
       ✅ 이미지: modelId가 이미 있으니 modelId로 바로 con 조회(추가쿼리 X)
    ========================================================= */
    public UniqueUpsellCandidatesPageDto buildCandidatesPage(Long pickedTrimId, Long addPrice) {

        long finalAddPrice = normalizeAddPrice(addPrice);
        boolean isUnlimited = (finalAddPrice == ADD_UNLIMITED);

        CarTrimRepository.PickedTrimDetailView picked =
                carTrimRepository.findPickedTrimDetail(pickedTrimId);

        if (picked == null) {
            return new UniqueUpsellCandidatesPageDto(null, List.of(), finalAddPrice, isUnlimited);
        }

        UniqueUpsellCarCardDto pickedDto =
                new UniqueUpsellCarCardDto(
                        picked.getTrimId(),
                        picked.getModelId(),
                        picked.getBrandName(),
                        picked.getModelName(),
                        picked.getTrimName(),
                        picked.getBasePrice(),
                        resolveConImageOrDefaultByModelId(picked.getModelId()) // ✅ con 이미지
                );

        // ✅ 제한없음이면 DB에 저장된 차량 중 제일 비싼 5대(모델별 최고가 트림)
        if (isUnlimited) {
            List<CarTrimRepository.UpsellModelMinTrimView> topRows =
                    carTrimRepository.findTopExpensiveModelTrimsExcludeTrimId(pickedTrimId, CANDIDATE_LIMIT);

            List<UniqueUpsellCarCardDto> candidates = topRows.stream()
                    .map(v -> new UniqueUpsellCarCardDto(
                            v.getTrimId(),
                            v.getModelId(),
                            v.getBrandName(),
                            v.getModelName(),
                            v.getTrimName(),
                            v.getBasePrice(),
                            resolveConImageOrDefaultByModelId(v.getModelId()) // ✅ con 이미지
                    ))
                    .toList();

            return new UniqueUpsellCandidatesPageDto(pickedDto, candidates, finalAddPrice, true);
        }

        long pickedBasePrice = picked.getBasePrice();
        long upperPrice = pickedBasePrice + finalAddPrice;

        String excludeModelNameKey = picked.getModelName() == null
                ? ""
                : picked.getModelName().trim().toUpperCase();

        List<CarTrimRepository.UpsellModelMinTrimView> rows =
                carTrimRepository.findUpsellModelMinTrimsInRangeExcludeModelNameKey(
                        pickedBasePrice,
                        upperPrice,
                        picked.getSegment(),
                        picked.getModelId(),
                        excludeModelNameKey,
                        CANDIDATE_LIMIT
                );

        List<UniqueUpsellCarCardDto> candidates = rows.stream()
                .map(v -> new UniqueUpsellCarCardDto(
                        v.getTrimId(),
                        v.getModelId(),
                        v.getBrandName(),
                        v.getModelName(),
                        v.getTrimName(),
                        v.getBasePrice(),
                        resolveConImageOrDefaultByModelId(v.getModelId()) // ✅ con 이미지
                ))
                .toList();

        return new UniqueUpsellCandidatesPageDto(pickedDto, candidates, finalAddPrice, false);
    }

    /* =========================================================
       Step3 : compare page
       ✅ 이미지: modelId가 있으니 modelId로 바로 con 조회
    ========================================================= */
    public UniqueComparePageDto buildComparePage(Long leftTrimId, Long rightTrimId) {

        CarTrimRepository.PickedTrimDetailView left =
                carTrimRepository.findPickedTrimDetail(leftTrimId);

        CarTrimRepository.PickedTrimDetailView right =
                carTrimRepository.findPickedTrimDetail(rightTrimId);

        UniqueUpsellCarCardDto leftDto = left == null ? null :
                new UniqueUpsellCarCardDto(
                        left.getTrimId(),
                        left.getModelId(),
                        left.getBrandName(),
                        left.getModelName(),
                        left.getTrimName(),
                        left.getBasePrice(),
                        resolveConImageOrDefaultByModelId(left.getModelId()) // ✅ con 이미지
                );

        UniqueUpsellCarCardDto rightDto = right == null ? null :
                new UniqueUpsellCarCardDto(
                        right.getTrimId(),
                        right.getModelId(),
                        right.getBrandName(),
                        right.getModelName(),
                        right.getTrimName(),
                        right.getBasePrice(),
                        resolveConImageOrDefaultByModelId(right.getModelId()) // ✅ con 이미지
                );

        return new UniqueComparePageDto(leftDto, rightDto);
    }

    /* =========================================================
       ✅ 이미지 resolver
       - con(view_type='con') 1장 가져오고 없으면 fallback
       - pick 화면은 UpsellCardView에 modelId가 없어서 trimId -> modelId 조회
    ========================================================= */

    private String resolveConImageOrDefaultByTrimId(Long trimId) {
        if (trimId == null) return DEFAULT_MAIN_IMG;

        Long modelId = carTrimRepository.findModelIdByTrimId(trimId)
                .map(v -> v.getModelId())
                .orElse(null);

        return resolveConImageOrDefaultByModelId(modelId);
    }

    private String resolveConImageOrDefaultByModelId(Long modelId) {
        if (modelId == null) return DEFAULT_MAIN_IMG;

        return carImageRepository
                .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(modelId, "con")
                .map(img -> img.getImageUrl())
                .filter(v -> v != null && !v.trim().isEmpty())
                .orElse(DEFAULT_MAIN_IMG);
    }

    private long normalizeAddPrice(Long addPrice) {
        if (addPrice == null) return ADD_5M;

        long v = addPrice;

        // ✅ 제한없음 값은 그대로 통과
        if (v == ADD_UNLIMITED) return ADD_UNLIMITED;

        if (v == ADD_5M || v == ADD_10M || v == ADD_15M || v == ADD_20M) return v;
        return ADD_5M;
    }
}
