package com.carproject.car.service;

import com.carproject.car.dto.QuoteListItemDto;
import com.carproject.car.dto.QuoteRequestDto;
import com.carproject.car.dto.QuoteViewDto;
import com.carproject.car.dto.QuoteViewDto.QuoteOptionLineDto;
import com.carproject.car.entity.CarTrim;
import com.carproject.car.entity.OptionItem;
import com.carproject.car.entity.OptionPackageItem;
import com.carproject.car.entity.OptionType;
import com.carproject.car.entity.TrimColor;
import com.carproject.car.repository.CarImageRepository;
import com.carproject.car.repository.CarTrimRepository;
import com.carproject.car.repository.OptionDependencyRepository;
import com.carproject.car.repository.OptionItemRepository;
import com.carproject.car.repository.OptionPackageItemRepository;
import com.carproject.car.repository.TrimColorRepository;
import com.carproject.car.repository.TrimOptionRepository;
import com.carproject.global.common.entity.Yn;
import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import com.carproject.quote.entity.Quote;
import com.carproject.quote.entity.QuoteEvent;
import com.carproject.quote.entity.QuoteOption;
import com.carproject.quote.entity.QuoteStatus;
import com.carproject.quote.repository.QuoteEventRepository;
import com.carproject.quote.repository.QuoteOptionRepository;
import com.carproject.quote.repository.QuoteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private static final String VIEW_TYPE_EXTERIOR = "exterior"; // ✅ 하드코딩 방지(추후 공통 상수로 분리 가능)

    private final QuoteRepository quoteRepository;
    private final QuoteOptionRepository quoteOptionRepository;
    private final QuoteEventRepository quoteEventRepository;

    private final MemberRepository memberRepository;
    private final CarTrimRepository carTrimRepository;
    private final TrimColorRepository trimColorRepository;
    private final TrimOptionRepository trimOptionRepository;
    private final OptionItemRepository optionItemRepository;
    private final OptionPackageItemRepository optionPackageItemRepository;
    private final OptionDependencyRepository optionDependencyRepository;

    // ✅ 견적서/견적리스트 이미지 출력용
    private final CarImageRepository carImageRepository;

    // ✅ 할인 계산은 전용 서비스로 위임 (MultipleBagFetchException 회피 구조)
    private final QuoteDiscountService quoteDiscountService;

    private final EntityManager entityManager;

    // =========================================================
    // 견적 제출
    // =========================================================
    @Transactional
    public Long submitQuote(Long memberId, QuoteRequestDto req) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음. memberId=" + memberId));

        CarTrim trim = carTrimRepository.findById(req.getTrimId())
                .orElseThrow(() -> new EntityNotFoundException("트림 없음. trimId=" + req.getTrimId()));

        // =========================================================
        // ✅ [C] 요청값 정합성 검증(호환 유지용 안전벨트)
        // - req.modelId / req.variantId가 들어온 경우에만 검증
        // =========================================================
        if (req.getVariantId() != null) {
            Long actualVariantId = (trim.getVariant() != null) ? trim.getVariant().getVariantId() : null;
            if (!Objects.equals(req.getVariantId(), actualVariantId)) {
                throw new IllegalArgumentException(
                        "요청 variantId와 trim의 variantId가 일치하지 않습니다. " +
                                "req=" + req.getVariantId() + ", actual=" + actualVariantId
                );
            }
        }

        if (req.getModelId() != null) {
            Long actualModelId = (trim.getVariant() != null && trim.getVariant().getModel() != null)
                    ? trim.getVariant().getModel().getModelId()
                    : null;

            if (!Objects.equals(req.getModelId(), actualModelId)) {
                throw new IllegalArgumentException(
                        "요청 modelId와 trim의 modelId가 일치하지 않습니다. " +
                                "req=" + req.getModelId() + ", actual=" + actualModelId
                );
            }
        }

        // ✅ trimColorId null 방어 (NPE/IllegalArgument 방지)
        if (req.getTrimColorId() == null) {
            throw new IllegalArgumentException("trimColorId는 필수입니다.");
        }

        TrimColor trimColor = trimColorRepository.findById(req.getTrimColorId())
                .orElseThrow(() -> new EntityNotFoundException("컬러 없음. trimColorId=" + req.getTrimColorId()));

        if (trimColor.getTrim() == null || !Objects.equals(trimColor.getTrim().getTrimId(), trim.getTrimId())) {
            throw new IllegalArgumentException("선택한 컬러가 해당 트림에 속하지 않습니다.");
        }

        // -------------------------
        // 기본 가격
        // -------------------------
        BigDecimal basePrice = nvl(trim.getBasePrice());

        // ✅ 컬러 가격 계산 (중복/가독성 개선)
        BigDecimal colorPrice = BigDecimal.ZERO;
        if (trimColor.getColor() != null) {
            colorPrice = nvl(trimColor.getColor().getColorPrice());
        }

        // -------------------------
        // 사용자 선택 옵션 (가변 리스트로)
        // -------------------------
        List<Long> packageIds = new ArrayList<>(distinct(req.getPackageOptionIds()));
        List<Long> singleIds  = new ArrayList<>(distinct(req.getSingleOptionIds()));

        // -------------------------
        // 트림 허용 옵션 / 필수 옵션
        // -------------------------
        Set<Long> allowedOptionIds =
                new HashSet<>(trimOptionRepository.findAllowedOptionIdsByTrimId(trim.getTrimId()));

        Set<Long> requiredOptionIds =
                new HashSet<>(trimOptionRepository.findRequiredOptionIdsByTrimId(trim.getTrimId(), Yn.Y));

        // 필수옵션은 저장 대상에서 제거
        packageIds.removeIf(requiredOptionIds::contains);
        singleIds.removeIf(requiredOptionIds::contains);

        // 패키지가 단일에 섞이면 제거
        singleIds.removeAll(packageIds);

        // 허용옵션 검증
        for (Long pkgId : packageIds) {
            if (!allowedOptionIds.contains(pkgId)) {
                throw new IllegalArgumentException("해당 트림에서 허용되지 않은 패키지입니다. optionItemId=" + pkgId);
            }
        }
        for (Long singleId : singleIds) {
            if (!allowedOptionIds.contains(singleId)) {
                throw new IllegalArgumentException("해당 트림에서 허용되지 않은 옵션입니다. optionItemId=" + singleId);
            }
        }

        // -------------------------
        // 패키지 → child 옵션 로딩(IS_INCLUDED=Y)
        // -------------------------
        Map<Long, Set<Long>> packageChildren = loadPackageChildren(packageIds);

        // 포함옵션이 트림 허용옵션이 아니면 제거(방어)
        for (Map.Entry<Long, Set<Long>> e : packageChildren.entrySet()) {
            e.getValue().removeIf(childId -> !allowedOptionIds.contains(childId));
        }

        // -------------------------
        // OPTION_ITEM 로딩
        // -------------------------
        Set<Long> loadIds = new HashSet<>();
        loadIds.addAll(packageIds);
        loadIds.addAll(singleIds);
        packageChildren.values().forEach(loadIds::addAll);

        Map<Long, OptionItem> optionById = optionItemRepository.findAllById(loadIds).stream()
                .collect(Collectors.toMap(OptionItem::getOptionItemId, o -> o));

        // -------------------------
        // Quote 생성
        // -------------------------
        Quote quote = new Quote();
        quote.setMember(member);
        quote.setTrim(trim);
        quote.setTrimColorId(trimColor.getTrimColorId());
        quote.setStatus(QuoteStatus.SUBMITTED);

        quote.setBasePrice(basePrice);
        quote.setOptionPrice(BigDecimal.ZERO);
        quote.setDiscountPrice(BigDecimal.ZERO);
        quote.setTotalPrice(BigDecimal.ZERO);

        quoteRepository.save(quote);

        // -------------------------
        // QuoteOption 구성
        // -------------------------
        BigDecimal optionPrice = BigDecimal.ZERO;
        Map<Long, QuoteOption> qoMap = new LinkedHashMap<>();

        // (A) 패키지 포함 옵션
        Set<Long> includedSingles = new HashSet<>();
        for (Map.Entry<Long, Set<Long>> e : packageChildren.entrySet()) {
            Long pkgId = e.getKey();
            OptionItem pkg = must(optionById, pkgId, "패키지 옵션 없음 optionItemId=" + pkgId);

            for (Long childId : e.getValue()) {
                includedSingles.add(childId);
                OptionItem child = must(optionById, childId, "포함 옵션 없음 optionItemId=" + childId);

                QuoteOption qo = new QuoteOption();
                qo.setQuote(quote);
                qo.setOptionItem(child);
                qo.setPackageOptionItem(pkg);
                qo.setOptionPrice(BigDecimal.ZERO);

                qoMap.putIfAbsent(childId, qo);
            }
        }

        // (B) 단일 옵션
        for (Long id : singleIds) {
            if (includedSingles.contains(id)) continue;

            OptionItem oi = must(optionById, id, "단일 옵션 없음 optionItemId=" + id);

            QuoteOption qo = new QuoteOption();
            qo.setQuote(quote);
            qo.setOptionItem(oi);
            qo.setPackageOptionItem(null);
            qo.setOptionPrice(nvl(oi.getOptionPrice()));

            if (qoMap.putIfAbsent(id, qo) == null) {
                optionPrice = optionPrice.add(nvl(oi.getOptionPrice()));
            }
        }

        // (C) 패키지 본체
        for (Long id : packageIds) {
            OptionItem oi = must(optionById, id, "패키지 옵션 없음 optionItemId=" + id);

            QuoteOption qo = new QuoteOption();
            qo.setQuote(quote);
            qo.setOptionItem(oi);
            qo.setPackageOptionItem(null);
            qo.setOptionPrice(nvl(oi.getOptionPrice()));

            if (qoMap.putIfAbsent(id, qo) == null) {
                optionPrice = optionPrice.add(nvl(oi.getOptionPrice()));
            }
        }

        // 저장
        for (QuoteOption qo : qoMap.values()) {
            quoteOptionRepository.save(qo);
        }

        // =========================================================
        // ✅ 할인 적용 (전용 서비스 사용)
        // =========================================================
        BigDecimal subtotal = basePrice.add(colorPrice).add(optionPrice);

        QuoteDiscountService.DiscountResult dr =
                quoteDiscountService.calculate(trim, subtotal);

        BigDecimal discountPrice = nvl(dr.totalDiscountPrice());
        if (discountPrice.compareTo(subtotal) > 0) discountPrice = subtotal;

        BigDecimal totalPrice = subtotal.subtract(discountPrice);

        quote.setOptionPrice(optionPrice);
        quote.setDiscountPrice(discountPrice);
        quote.setTotalPrice(totalPrice);

        // 이벤트별 할인 저장
        for (QuoteDiscountService.AppliedEventDiscount a : dr.appliedEvents()) {
            QuoteEvent qe = new QuoteEvent();
            qe.setQuote(quote);
            qe.setEvent(entityManager.getReference(com.carproject.event.entity.Event.class, a.eventId()));
            qe.setDiscountPrice(a.discountPrice());
            quoteEventRepository.save(qe);
        }

        return quote.getQuoteId();
    }

    // =========================================================
    // 조회
    // =========================================================
    @Transactional(readOnly = true)
    public QuoteViewDto getQuoteView(Long memberId, Long quoteId) {

        Quote q = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("견적 없음. quoteId=" + quoteId));

        if (q.getMember() == null || !Objects.equals(q.getMember().getMemberId(), memberId)) {
            throw new IllegalArgumentException("권한 없음");
        }

        List<QuoteOption> opts = quoteOptionRepository.findByQuote_QuoteId(quoteId);

        Map<Long, String> packageNameById = new HashMap<>();
        for (QuoteOption qo : opts) {
            OptionItem oi = qo.getOptionItem();
            if (oi == null) continue;

            if (qo.getPackageOptionItem() == null && oi.getOptionType() == OptionType.PACKAGE) {
                packageNameById.put(oi.getOptionItemId(), oi.getOptionName());
            }
        }

        List<QuoteOptionLineDto> lines = new ArrayList<>();
        for (QuoteOption qo : opts) {
            OptionItem oi = qo.getOptionItem();
            if (oi == null) continue;

            String includedBy = null;
            if (qo.getPackageOptionItem() != null && qo.getPackageOptionItem().getOptionItemId() != null) {
                includedBy = packageNameById.getOrDefault(
                        qo.getPackageOptionItem().getOptionItemId(),
                        qo.getPackageOptionItem().getOptionName()
                );
            }

            lines.add(new QuoteOptionLineDto(
                    oi.getOptionName(),
                    oi.getOptionType() != null ? oi.getOptionType().name() : "-",
                    nvl(qo.getOptionPrice()),
                    includedBy
            ));
        }

        // ✅ MODEL 기준 exterior 이미지 1장 (COLOR_ID 무시)
        Long modelId = q.getTrim().getVariant().getModel().getModelId();
        String exteriorUrl = carImageRepository
                .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(modelId, VIEW_TYPE_EXTERIOR)
                .map(img -> normalizeWebPath(img.getImageUrl()))
                .orElse(null);

        // =========================================================
        // ✅ [D] trimColor null-safe (조회단 NPE 방어)
        // =========================================================
        TrimColor tc = q.getTrimColor();

        String colorName = "-";
        BigDecimal colorPrice = BigDecimal.ZERO;

        if (tc != null && tc.getColor() != null) {
            if (tc.getColor().getColorName() != null) {
                colorName = tc.getColor().getColorName();
            }
            colorPrice = nvl(tc.getColor().getColorPrice());
        }

        return new QuoteViewDto(
                q.getQuoteId(),
                q.getTrim().getVariant().getModel().getBrand().getBrandName(),
                q.getTrim().getVariant().getModel().getModelName(),
                q.getTrim().getVariant().getEngineType(),
                q.getTrim().getVariant().getEngineName(),
                q.getTrim().getTrimName(),
                colorName,
                exteriorUrl,
                nvl(q.getBasePrice()),
                colorPrice,
                nvl(q.getOptionPrice()),
                nvl(q.getDiscountPrice()),
                nvl(q.getTotalPrice()),
                lines
        );
    }

    @Transactional(readOnly = true)
    public List<QuoteListItemDto> getMyQuotes(Long memberId) {
        List<QuoteListItemDto> list = quoteRepository.findMyQuoteList(memberId);

        // ✅ 각 카드에 exterior 이미지 1장 세팅 (MODEL 기준)
        for (QuoteListItemDto dto : list) {
            if (dto.getModelId() == null) continue;

            String url = carImageRepository
                    .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(dto.getModelId(), VIEW_TYPE_EXTERIOR)
                    .map(img -> normalizeWebPath(img.getImageUrl()))
                    .orElse(null);

            dto.setImageUrl(url);
        }

        return list;
    }

    // =========================================================
    // 삭제
    // =========================================================
    @Transactional
    public void deleteQuote(Long memberId, Long quoteId) {

        Quote q = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("견적 없음. quoteId=" + quoteId));

        if (q.getMember() == null || !Objects.equals(q.getMember().getMemberId(), memberId)) {
            throw new IllegalArgumentException("권한 없음");
        }

        // 자식 먼저 삭제 (FK 제약 방어)
        quoteEventRepository.deleteByQuote_QuoteId(quoteId);
        quoteOptionRepository.deleteByQuote_QuoteId(quoteId);

        quoteRepository.delete(q);
    }

    // =========================================================
    // helpers
    // =========================================================
    private Map<Long, Set<Long>> loadPackageChildren(List<Long> pkgIds) {
        if (pkgIds == null || pkgIds.isEmpty()) return Map.of();

        Map<Long, Set<Long>> map = new LinkedHashMap<>();
        List<OptionPackageItem> rows =
                optionPackageItemRepository.findIncludedChildrenByPackageIds(pkgIds);

        for (OptionPackageItem r : rows) {
            if (r.getPackageOptionItem() == null || r.getChildOptionItem() == null) continue;

            Long pkgId = r.getPackageOptionItem().getOptionItemId();
            Long childId = r.getChildOptionItem().getOptionItemId();
            if (pkgId == null || childId == null) continue;

            map.computeIfAbsent(pkgId, k -> new LinkedHashSet<>()).add(childId);
        }
        return map;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 템플릿에서 경로가 깨지지 않게 / prefix 보정 */
    private String normalizeWebPath(String url) {
        if (url == null) return null;

        String u = url.trim();
        if (u.isEmpty()) return null;

        // ✅ Windows 경로 방어 (\ → /)
        u = u.replace("\\", "/");

        // ✅ 절대 URL은 그대로 사용
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return u;
        }

        // ✅ 웹 경로 보장
        if (!u.startsWith("/")) {
            u = "/" + u;
        }

        return u;
    }

    private static <T> List<T> distinct(List<T> list) {
        if (list == null) return List.of();
        return list.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static <K, V> V must(Map<K, V> map, K key, String msg) {
        V v = map.get(key);
        if (v == null) throw new EntityNotFoundException(msg);
        return v;
    }
}
