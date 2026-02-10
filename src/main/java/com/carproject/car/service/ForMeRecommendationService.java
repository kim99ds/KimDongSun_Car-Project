package com.carproject.car.service;

import com.carproject.car.dto.*;
import com.carproject.car.entity.CarImage;
import com.carproject.car.entity.CarModel;
import com.carproject.car.entity.CarVariant;
import com.carproject.car.repository.CarImageRepository;
import com.carproject.car.repository.CarModelRepository;
import com.carproject.car.repository.CarTrimRepository;
import com.carproject.car.repository.ModelCountView;
import com.carproject.car.repository.ModelIdView;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForMeRecommendationService {

    private final CarModelRepository carModelRepository;
    private final CarTrimRepository carTrimRepository;

    // ✅ 이미지 조회용 repository
    private final CarImageRepository carImageRepository;

    // 예) "12.3", "12.3km/L" 등에서 숫자 파싱
    private static final Pattern NUM = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    public List<ForMeRecommendDto> recommend(ForMeAnswerDto answers, int topN) {

        // 1) 후보 로드
        List<CarModel> models = carModelRepository.findAllWithBrandAndVariants();

        // 2) 모델별 최저가
        Map<Long, BigDecimal> modelMinPrices = new HashMap<>();
        for (com.carproject.car.repository.ModelMinPriceView view : carTrimRepository.findModelMinPrices()) {
            modelMinPrices.put(view.getModelId(), view.getMinBasePrice());
        }

        // 3) 6/7인승 가능 모델 set
        Set<Long> seatCapableModelIds = new HashSet<>();
        for (ModelIdView v : carTrimRepository.findSeatCapableModelIds()) {
            if (v.getModelId() != null) seatCapableModelIds.add(v.getModelId());
        }

        // 4) assistant 옵션 개수 map
        Map<Long, Long> assistantCounts = new HashMap<>();
        for (ModelCountView v : carTrimRepository.findAssistantOptionCountsByModel()) {
            assistantCounts.put(v.getModelId(), v.getCnt() == null ? 0L : v.getCnt());
        }

        // 5) 답변 정규화
        List<Powertrain> desired = normalizePowertrains(answers.getPowertrains());
        boolean wantsEv = desired.contains(Powertrain.EV);
        boolean evChargingOk = Boolean.TRUE.equals(answers.getEvChargingAvailable());

        FuelEconomyPriority fuelPri = answers.getFuelEconomyPriority() == null
                ? FuelEconomyPriority.NORMAL
                : answers.getFuelEconomyPriority();

        OriginPreference originPref = answers.getOriginPreference() == null
                ? OriginPreference.ANY
                : answers.getOriginPreference();

        AssistantPriority asstPri = answers.getAssistantPriority() == null
                ? AssistantPriority.NORMAL
                : answers.getAssistantPriority();

        // 6) 스코어링
        List<Scored> scored = new ArrayList<>();
        for (CarModel m : models) {
            BigDecimal minPrice = modelMinPrices.getOrDefault(
                    m.getModelId(),
                    BigDecimal.valueOf(Long.MAX_VALUE)
            );

            int score = scoreModel(
                    answers,
                    desired,
                    wantsEv,
                    evChargingOk,
                    fuelPri,
                    originPref,
                    asstPri,
                    seatCapableModelIds,
                    assistantCounts,
                    m,
                    minPrice
            );

            scored.add(new Scored(m, minPrice, score));
        }

        // 7) 정렬: score → likeCount → viewCount → modelYear
        scored.sort((a, b) -> {
            int c = Integer.compare(b.score, a.score);
            if (c != 0) return c;
            c = Long.compare(nullSafe(b.model.getLikeCount()), nullSafe(a.model.getLikeCount()));
            if (c != 0) return c;
            c = Long.compare(nullSafe(b.model.getViewCount()), nullSafe(a.model.getViewCount()));
            if (c != 0) return c;
            return Integer.compare(nullSafe(b.model.getModelYear()), nullSafe(a.model.getModelYear()));
        });

        // 8) topN 반환
        int limit = Math.min(topN, scored.size());
        List<ForMeRecommendDto> result = new ArrayList<>(limit);

        for (int i = 0; i < limit; i++) {
            Scored s = scored.get(i);
            int matchRate = clamp(s.score, 0, 100);

            // ✅ 이미지 URL 조회 (Optional.map 안 써도 됨)
            Optional<CarImage> optImage =
                    carImageRepository.findFirstByModel_ModelIdOrderByImageIdAsc(s.model.getModelId());

            String imageUrl = optImage.isPresent()
                    ? optImage.get().getImageUrl()
                    : "/main/images/bast_car_default.png";

            // ✅ DTO 생성자 순서가 “DTO 파일”과 정확히 같아야 함
            result.add(new ForMeRecommendDto(
                    s.model.getModelId(),
                    s.model.getBrand().getBrandName(),
                    s.model.getModelName(),
                    s.model.getSegment(),
                    imageUrl,      // ✅ String은 여기!
                    s.minPrice,    // ✅ BigDecimal은 여기!
                    clamp(s.score, 0, 100),
                    matchRate
            ));
        }

        return result;
    }

    private int scoreModel(
            ForMeAnswerDto answers,
            List<Powertrain> desired,
            boolean wantsEv,
            boolean evChargingOk,
            FuelEconomyPriority fuelPri,
            OriginPreference originPref,
            AssistantPriority asstPri,
            Set<Long> seatCapableModelIds,
            Map<Long, Long> assistantCounts,
            CarModel m,
            BigDecimal minPrice
    ) {
        int total = 0;

        // 1) 탑승 + 6/7인승 옵션가점 (15)
        total += scorePassengersPlusSeatBonus(
                answers.getPassengers(), m.getModelId(), m.getSegment(), seatCapableModelIds
        );

        // 2) 짐 적재량(세그먼트만) (10)
        total += scoreLuggage(answers.getLuggage(), m.getSegment());

        // 3) 예산 (20)
        total += scoreBudget(answers.getBudget(), minPrice);

        // 4) 파워트레인 (20) + EV 충전불가 패널티
        total += scorePowertrain(desired, wantsEv, evChargingOk, m.getVariants());

        // 6) 주행환경 (10)
        total += scoreDrivingEnv(answers.getDrivingEnv(), m.getSegment(), m.getVariants());

        // 7) 연비 중요도 (10)
        total += scoreFuelEconomy(fuelPri, m.getVariants());

        // 8) 국산/외제 선호 (10)
        total += scoreOrigin(originPref, safeUpper(m.getBrand().getCountryCode()));

        // 9) assistant 옵션 중요도 (5)
        long asstCnt = assistantCounts.getOrDefault(m.getModelId(), 0L);
        total += scoreAssistant(asstPri, asstCnt);

        return clamp(total, 0, 100);
    }

    // ------------------------------
    // scoring rules
    // ------------------------------

    private int scorePassengersPlusSeatBonus(Integer passengers, Long modelId, String segment, Set<Long> seatCapableModelIds) {
        int p = passengers == null ? 0 : passengers;
        SegmentType seg = SegmentType.of(segment);

        int base;
        if (p >= 5) {
            base = switch (seg) {
                case SUV, BOXCAR -> 10;
                case SEDAN -> 6;
                case COMPACT -> 4;
                default -> 5;
            };
        } else if (p <= 2) {
            base = switch (seg) {
                case COMPACT -> 10;
                case SEDAN -> 7;
                case SUV, BOXCAR -> 5;
                default -> 6;
            };
        } else { // 3~4
            base = switch (seg) {
                case SEDAN -> 10;
                case SUV, BOXCAR -> 9;
                case COMPACT -> 7;
                default -> 8;
            };
        }

        int bonus = 0;
        if (p >= 6 && modelId != null && seatCapableModelIds.contains(modelId)) {
            bonus = 5;
        }

        return clamp(base + bonus, 0, 15);
    }

    private int scoreLuggage(LuggageLevel luggage, String segment) {
        SegmentType seg = SegmentType.of(segment);
        LuggageLevel l = luggage == null ? LuggageLevel.NONE : luggage;

        return switch (l) {
            case HEAVY -> switch (seg) {
                case SUV, BOXCAR -> 10;
                case SEDAN -> 7;
                case COMPACT -> 4;
                default -> 6;
            };
            case MEDIUM -> switch (seg) {
                case SUV, BOXCAR -> 10;
                case SEDAN -> 9;
                case COMPACT -> 6;
                default -> 8;
            };
            case LIGHT -> switch (seg) {
                case SEDAN -> 10;
                case SUV, BOXCAR -> 9;
                case COMPACT -> 8;
                default -> 8;
            };
            case NONE -> switch (seg) {
                case COMPACT -> 10;
                case SEDAN -> 9;
                case SUV, BOXCAR -> 7;
                default -> 8;
            };
        };
    }

    private int scoreBudget(BudgetRange range, BigDecimal minPrice) {
        if (range == null || minPrice == null) return 0;
        long won;
        try {
            won = minPrice.longValue();
        } catch (Exception e) {
            return 0;
        }
        return range.contains(won) ? 20 : 0;
    }

    private int scorePowertrain(List<Powertrain> desired, boolean wantsEv, boolean evChargingOk, List<CarVariant> variants) {
        if (desired == null || desired.isEmpty() || desired.contains(Powertrain.ANY)) return 10;

        Set<Powertrain> modelTypes = new HashSet<>();
        for (CarVariant v : safe(variants)) {
            modelTypes.add(mapEngineType(v.getEngineType()));
        }

        int penalty = 0;
        if (wantsEv && !evChargingOk && modelTypes.contains(Powertrain.EV)) {
            penalty = -20;
        }

        List<Powertrain> matchingDesired = desired;
        if (wantsEv && !evChargingOk) {
            matchingDesired = desired.stream().filter(p -> p != Powertrain.EV).toList();
        }

        boolean matched = false;
        for (Powertrain p : matchingDesired) {
            if (modelTypes.contains(p)) {
                matched = true;
                break;
            }
        }

        int base = matched ? 20 : 0;
        return base + penalty;
    }

    private int scoreDrivingEnv(DrivingEnvironment env, String segment, List<CarVariant> variants) {
        DrivingEnvironment e = env == null ? DrivingEnvironment.MIXED : env;
        SegmentType seg = SegmentType.of(segment);

        boolean hasAwd = hasAwd(variants);

        return switch (e) {
            case CITY_COMMUTE -> switch (seg) {
                case COMPACT -> 10;
                case SEDAN -> 8;
                case SUV, BOXCAR -> 6;
                default -> 7;
            };
            case HIGHWAY_LONG -> switch (seg) {
                case SEDAN -> 10;
                case SUV, BOXCAR -> 8;
                case COMPACT -> 7;
                default -> 7;
            };
            case LEISURE_OUTDOOR -> {
                int s = 0;
                if (seg == SegmentType.SUV || seg == SegmentType.BOXCAR) s += 7;
                if (hasAwd) s += 3;
                yield clamp(s, 0, 10);
            }
            case MIXED -> 6;
        };
    }

    private int scoreFuelEconomy(FuelEconomyPriority pri, List<CarVariant> variants) {
        FuelEconomyPriority p = pri == null ? FuelEconomyPriority.NORMAL : pri;
        double best = bestFuelEfficiency(variants);

        if (p == FuelEconomyPriority.NOT_IMPORTANT) return 0;
        if (p == FuelEconomyPriority.NORMAL) return 5;

        if (best <= 0) return 5;
        if (best >= 18.0) return 10;
        if (best >= 15.0) return 9;
        if (best >= 13.0) return 8;
        if (best >= 11.0) return 6;
        if (best >= 9.0) return 4;
        return 2;
    }

    private int scoreOrigin(OriginPreference pref, String countryCodeUpper) {
        OriginPreference p = pref == null ? OriginPreference.ANY : pref;
        if (p == OriginPreference.ANY) return 5;

        boolean isDomestic = "KOR".equals(countryCodeUpper);
        if (p == OriginPreference.DOMESTIC) return isDomestic ? 10 : 0;
        if (countryCodeUpper == null) return 0;
        return !isDomestic ? 10 : 0;
    }

    private int scoreAssistant(AssistantPriority pri, long assistantCount) {
        AssistantPriority p = pri == null ? AssistantPriority.NORMAL : pri;
        if (p == AssistantPriority.NOT_NEEDED) return 0;
        if (p == AssistantPriority.NORMAL) return 2;

        if (assistantCount >= 10) return 5;
        if (assistantCount >= 6) return 4;
        if (assistantCount >= 3) return 3;
        if (assistantCount >= 1) return 2;
        return 0;
    }

    // ------------------------------
    // helpers
    // ------------------------------

    private List<Powertrain> normalizePowertrains(List<Powertrain> powertrains) {
        if (powertrains == null || powertrains.isEmpty()) return List.of(Powertrain.ANY);
        return powertrains;
    }

    private long nullSafe(Long v) {
        return v == null ? 0L : v;
    }

    private int nullSafe(Integer v) {
        return v == null ? 0 : v;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase(Locale.ROOT);
        return t.isEmpty() ? null : t;
    }

    private boolean hasAwd(List<CarVariant> variants) {
        for (CarVariant v : safe(variants)) {
            String dt = v.getDriveType();
            if (dt == null) continue;
            String u = dt.toUpperCase(Locale.ROOT);
            if (u.contains("AWD") || u.contains("4WD") || u.contains("4-WD") || u.contains("4X4")) return true;
            if (u.contains("사륜") || u.contains("4륜")) return true;
        }
        return false;
    }

    private double bestFuelEfficiency(List<CarVariant> variants) {
        double best = -1;
        for (CarVariant v : safe(variants)) {
            String s = v.getFuelEfficiency();
            double val = parseFirstDouble(s);
            if (val > best) best = val;
        }
        return best;
    }

    private double parseFirstDouble(String s) {
        if (s == null) return -1;
        Matcher m = NUM.matcher(s);
        if (!m.find()) return -1;
        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return -1;
        }
    }

    private Powertrain mapEngineType(String engineType) {
        if (engineType == null) return Powertrain.GASOLINE;
        String u = engineType.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (u.contains("EV") || u.contains("ELECTRIC") || u.contains("전기")) return Powertrain.EV;
        if (u.contains("PHEV") || u.contains("PLUGIN") || u.contains("플러그")) return Powertrain.PHEV;
        if (u.contains("HYBRID") || u.contains("HEV") || u.contains("하이브")) return Powertrain.HYBRID;
        if (u.contains("DIESEL") || u.contains("디젤")) return Powertrain.DIESEL;
        return Powertrain.GASOLINE;
    }

    private enum SegmentType {
        SUV, SEDAN, COMPACT, BOXCAR, OTHER;

        static SegmentType of(String segment) {
            if (segment == null) return OTHER;
            String u = segment.toUpperCase(Locale.ROOT);

            if (u.contains("BOX") || u.contains("BOXCAR") || u.contains("박스")) return BOXCAR;
            if (u.contains("SUV") || u.contains("CUV")) return SUV;
            if (u.contains("SEDAN") || u.contains("세단")) return SEDAN;
            if (u.contains("HATCH") || u.contains("COMPACT") || u.contains("소형") || u.contains("준중형") || u.contains("경형")) return COMPACT;
            return OTHER;
        }
    }

    private static class Scored {
        final CarModel model;
        final BigDecimal minPrice;
        final int score;

        Scored(CarModel model, BigDecimal minPrice, int score) {
            this.model = model;
            this.minPrice = minPrice;
            this.score = score;
        }
    }
}
