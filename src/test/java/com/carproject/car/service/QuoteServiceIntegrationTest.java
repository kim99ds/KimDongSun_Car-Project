package com.carproject.car.service;

import com.carproject.car.dto.QuoteRequestDto;
import com.carproject.car.entity.*;
import com.carproject.car.repository.*;
import com.carproject.event.entity.*;
import com.carproject.event.repository.EventPolicyRepository;
import com.carproject.event.repository.EventRepository;
import com.carproject.event.repository.EventTargetRepository;
import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.repository.MemberRepository;
import com.carproject.quote.entity.Quote;
import com.carproject.quote.repository.QuoteEventRepository;
import com.carproject.quote.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuoteServiceIntegrationTest {

    @Autowired QuoteService quoteService;

    @Autowired MemberRepository memberRepository;

    @Autowired BrandRepository brandRepository;
    @Autowired CarModelRepository carModelRepository;
    @Autowired CarVariantRepository carVariantRepository;
    @Autowired CarTrimRepository carTrimRepository;

    @Autowired CarColorRepository carColorRepository;
    @Autowired TrimColorRepository trimColorRepository;

    @Autowired OptionItemRepository optionItemRepository;

    @Autowired QuoteRepository quoteRepository;
    @Autowired QuoteEventRepository quoteEventRepository;

    @Autowired EventRepository eventRepository;
    @Autowired EventTargetRepository eventTargetRepository;
    @Autowired EventPolicyRepository eventPolicyRepository;

    // ========= 공통 픽스처 =========
    private Member member;
    private CarTrim trim;
    private TrimColor trimColor;

    @BeforeEach
    void setUp() {
        // ✅ 실DB에 이미 있는 ACTIVE 이벤트(4개)는 삭제하지 않음 (영향 주면 위험)
        // 대신 "우리 테스트 이벤트가 항상 더 큰 할인(50%)"이 되도록 만들어서,
        // '최대 1개만 적용'을 확실히 검증한다.

        member = createMember("testUser_" + System.nanoTime());

        // 1) Brand 저장
        Brand brand = new Brand();
        brand.setBrandName("TEST_BRAND");
        brand = brandRepository.save(brand);

        // 2) Model 저장
        CarModel model = new CarModel();
        model.setBrand(brand);
        model.setModelName("TEST_MODEL");
        model.setSegment("SUV");
        model = carModelRepository.save(model);

        // 3) Variant 저장
        CarVariant variant = new CarVariant();
        variant.setModel(model);
        variant.setEngineType("GASOLINE");
        variant.setEngineName("2.0T");
        variant = carVariantRepository.save(variant);

        // 4) Trim 저장
        trim = new CarTrim();
        trim.setVariant(variant);
        trim.setTrimName("TEST_TRIM");
        trim.setBasePrice(new BigDecimal("30000000"));
        trim = carTrimRepository.save(trim);

        // 5) Color 저장
        CarColor color = new CarColor();
        color.setColorName("WHITE");
        color.setColorPrice(new BigDecimal("500000"));
        color = carColorRepository.save(color);

        // 6) TrimColor 저장
        trimColor = new TrimColor();
        trimColor.setTrim(trim);
        trimColor.setColor(color);
        trimColor = trimColorRepository.save(trimColor);
    }

    // ========= 테스트 1) 정상 견적 저장(옵션 없음) =========
    @Test
    void submitQuote_success_withoutOptions() {
        QuoteRequestDto req = new QuoteRequestDto();
        req.setTrimId(trim.getTrimId());
        req.setTrimColorId(trimColor.getTrimColorId());
        req.setPackageOptionIds(List.of());
        req.setSingleOptionIds(List.of());

        Long quoteId = quoteService.submitQuote(member.getMemberId(), req);

        Quote saved = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new AssertionError("quote not saved"));

        // ✅ 핵심: 저장 자체 성공 + 금액 관계식만 검증 (실DB ACTIVE 이벤트가 있으면 discount가 0이 아닐 수 있음)
        assertThat(saved.getBasePrice()).isNotNull();
        assertThat(saved.getOptionPrice()).isNotNull();
        assertThat(saved.getDiscountPrice()).isNotNull();
        assertThat(saved.getTotalPrice()).isNotNull();

        // total = (base + option) - discount  (프로젝트 현재 저장 방식 기준)
        assertThat(saved.getTotalPrice())
                .isEqualByComparingTo(saved.getBasePrice()
                        .add(saved.getOptionPrice())
                        .subtract(saved.getDiscountPrice()));
    }

    // ========= 테스트 2) 트림-컬러 불일치 차단 =========
    @Test
    void submitQuote_fail_whenTrimColorNotBelongsToTrim() {
        CarTrim otherTrim = new CarTrim();
        otherTrim.setVariant(trim.getVariant());
        otherTrim.setTrimName("OTHER_TRIM");
        otherTrim.setBasePrice(new BigDecimal("20000000"));
        otherTrim = carTrimRepository.save(otherTrim);

        TrimColor otherTrimColor = new TrimColor();
        otherTrimColor.setTrim(otherTrim);
        otherTrimColor.setColor(trimColor.getColor());
        otherTrimColor = trimColorRepository.save(otherTrimColor);

        QuoteRequestDto req = new QuoteRequestDto();
        req.setTrimId(trim.getTrimId()); // 기존 trim
        req.setTrimColorId(otherTrimColor.getTrimColorId()); // 다른 trim 소속 color
        req.setPackageOptionIds(List.of());
        req.setSingleOptionIds(List.of());

        assertThatThrownBy(() -> quoteService.submitQuote(member.getMemberId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 트림에 속하지");
    }

    // ========= 테스트 3) 허용되지 않은 옵션 차단 =========
    @Test
    void submitQuote_fail_whenOptionNotAllowed() {
        OptionItem oi = new OptionItem();
        oi.setOptionName("NOT_ALLOWED_OPTION");
        oi.setOptionType(OptionType.SINGLE);
        oi.setOptionPrice(new BigDecimal("1000000"));
        oi = optionItemRepository.save(oi);

        QuoteRequestDto req = new QuoteRequestDto();
        req.setTrimId(trim.getTrimId());
        req.setTrimColorId(trimColor.getTrimColorId());
        req.setSingleOptionIds(List.of(oi.getOptionItemId()));

        assertThatThrownBy(() -> quoteService.submitQuote(member.getMemberId(), req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않은 옵션");
    }

    // ========= 테스트 4) 이벤트 할인 "최대 1개" 적용 + (50%가 항상 선택됨) =========
    @Test
    void submitQuote_appliesOnlyBestOneEventDiscount_rate50Wins() {
        // ✅ 실DB에 ACTIVE 이벤트가 12% / 150만원 등이 있어도,
        // 50% 할인은 항상 더 크기 때문에 "최대 1개 선택" 로직 검증이 안정적.

        // 이벤트 A: 50% (RATE)
        Event best = createActiveAllEvent("TEST_RATE_50", DiscountType.RATE, new BigDecimal("50"));

        // 이벤트 B: 150만원 (PRICE) - 비교용
        Event small = createActiveAllEvent("TEST_PRICE_150", DiscountType.PRICE, new BigDecimal("1500000"));

        QuoteRequestDto req = new QuoteRequestDto();
        req.setTrimId(trim.getTrimId());
        req.setTrimColorId(trimColor.getTrimColorId());
        req.setPackageOptionIds(List.of());
        req.setSingleOptionIds(List.of());

        Long quoteId = quoteService.submitQuote(member.getMemberId(), req);
        Quote saved = quoteRepository.findById(quoteId).orElseThrow();

        // ✅ 기대 할인 = (base + option) * 50 / 100
        BigDecimal subtotal = saved.getBasePrice().add(saved.getOptionPrice());
        BigDecimal expectedDiscount = subtotal.multiply(new BigDecimal("50"))
                .divide(new BigDecimal("100")); // 50%

        // 1) 최대 할인(50%)이 선택되어야 함
        assertThat(saved.getDiscountPrice()).isEqualByComparingTo(expectedDiscount);

        // 2) QuoteEvent는 최대 1개만 저장되어야 함
        assertThat(quoteEventRepository.countByQuote_QuoteId(quoteId)).isEqualTo(1);

        // 3) total 관계식도 유지
        assertThat(saved.getTotalPrice())
                .isEqualByComparingTo(subtotal.subtract(saved.getDiscountPrice()));
    }

    // ========= helper methods =========

    private Member createMember(String loginId) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword("pw"); // 테스트용
        m.setName("테스트");
        m.setEmail(loginId + "@test.com");
        m.setBirthDate(LocalDate.of(1990, 1, 1));
        m.setStatus(MemberStatus.ACTIVE);
        return memberRepository.save(m);
    }

    private Event createActiveAllEvent(String title, DiscountType type, BigDecimal value) {
        Event e = new Event();
        e.setTitle(title);
        e.setStatus(EventStatus.ACTIVE);
        e.setStartDate(LocalDate.now().minusDays(1));
        e.setEndDate(LocalDate.now().plusDays(1));
        e.setCreatedBy(member); // NOT NULL
        e = eventRepository.save(e);

        EventTarget t = new EventTarget();
        t.setEvent(e);
        t.setTargetType(TargetType.ALL);
        t.setTargetValue("ALL");
        eventTargetRepository.save(t);

        EventPolicy p = new EventPolicy();
        p.setEvent(e);
        p.setDiscountType(type);     // PRICE or RATE
        p.setDiscountValue(value);   // PRICE: 금액, RATE: 퍼센트(예: 50)
        eventPolicyRepository.save(p);

        return e;
    }
}
