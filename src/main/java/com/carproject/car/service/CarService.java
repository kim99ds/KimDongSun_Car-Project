package com.carproject.car.service;

import com.carproject.car.entity.CarModel;
import com.carproject.car.repository.BrandRepository;
import com.carproject.car.repository.CarModelRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarModelRepository carModelRepository;
    private final BrandRepository brandRepository;

    /**
     * 기존 컨트롤러 호환용(그대로 사용 가능)
     */
    public List<CarModel> searchCars(
            String q,
            String segment,
            String brand,
            String engineType,
            String sort
    ) {
        return searchCarsWithSuggestion(q, false, segment, brand, engineType, sort).carModels();
    }

    /**
     * ✅ 검색어 자동교정(0건일 때만) + 제안 배너용 메타데이터 포함
     */
    public CarSearchResult searchCarsWithSuggestion(
            String q,
            boolean noSuggest,
            String segment,
            String brand,
            String engineType,
            String sort
    ) {
        String originalQ = normalizeNull(q);
        String segmentN = normalizeNull(segment);
        String brandN = normalizeNull(brand);
        String engineTypeN = normalizeNull(engineType);
        String sortN = normalizeNull(sort);

        // 1차 검색
        List<CarModel> result = carModelRepository.searchCars(originalQ, segmentN, brandN, engineTypeN, sortN);

        boolean didYouMeanApplied = false;
        String suggestedQ = null;

        // ✅ (A) 결과 0건일 때만 자동 교정 적용
        if (!noSuggest && originalQ != null && result.isEmpty()) {
            suggestedQ = suggestQuery(originalQ);

            if (suggestedQ != null && !suggestedQ.equalsIgnoreCase(originalQ)) {
                List<CarModel> suggestedResult = carModelRepository.searchCars(suggestedQ, segmentN, brandN, engineTypeN, sortN);
                if (!suggestedResult.isEmpty()) {
                    didYouMeanApplied = true;
                    result = suggestedResult;
                } else {
                    suggestedQ = null;
                }
            } else {
                suggestedQ = null;
            }
        }

        // ✅ 검색어가 있을 때(그리고 sort 지정이 없을 때) “일치률” 기반 정렬
        String effectiveQForRelevance = (didYouMeanApplied && suggestedQ != null) ? suggestedQ : originalQ;
        if (effectiveQForRelevance != null && sortN == null) {
            sortByRelevance(result, effectiveQForRelevance);
        }

        return new CarSearchResult(result, didYouMeanApplied, originalQ, suggestedQ);
    }

    public record CarSearchResult(
            List<CarModel> carModels,
            boolean didYouMeanApplied,
            String originalQ,
            String suggestedQ
    ) {
    }

    // =========================
    // Suggestion
    // =========================

    private String suggestQuery(String originalQ) {
        String q = originalQ.trim();
        if (q.isEmpty()) return null;

        // 1) 브랜드 별칭(영문/약칭 → 한글)
        String alias = brandAlias(q);
        if (alias != null) return alias;

        // 2) 모델/브랜드명 유사도 기반(레벤슈타인)
        List<String> candidates = new ArrayList<>();

        // 브랜드명은 개수가 적으니 전체
        var brands = brandRepository.findAll();
        for (var b : brands) {
            if (b.getBrandName() != null && !b.getBrandName().isBlank()) {
                candidates.add(b.getBrandName().trim());
            }
        }

        // 모델명은 distinct
        List<String> modelNames = carModelRepository.findDistinctModelNames();
        if (modelNames != null && !modelNames.isEmpty()) {
            candidates.addAll(modelNames);
        }

        // 후보가 너무 많으면 상한(안전장치)
        int limit = Math.min(candidates.size(), 5000);

        String best = null;
        double bestScore = 0.0;

        String nq = normalizeForCompare(q);
        for (int i = 0; i < limit; i++) {
            String cand = candidates.get(i);
            if (cand == null) continue;
            String nc = normalizeForCompare(cand);
            if (nc.isEmpty()) continue;

            double sim = similarity(nq, nc);
            if (sim > bestScore) {
                bestScore = sim;
                best = cand;
            }
        }

        // 너무 억지 교정 방지
        double threshold = (nq.length() <= 2) ? 0.90 : (nq.length() <= 4 ? 0.70 : 0.60);
        if (best != null && bestScore >= threshold) {
            return best;
        }
        return null;
    }

    private String brandAlias(String q) {
        String key = normalizeForCompare(q);
        Map<String, String> map = new HashMap<>();
        map.put("kia", "기아");
        map.put("kias", "기아");
        map.put("hyundai", "현대");
        map.put("genesis", "제네시스");
        map.put("bmw", "BMW");
        map.put("benz", "벤츠");
        map.put("mercedes", "벤츠");
        map.put("audi", "아우디");

        if (map.containsKey(key)) return map.get(key);
        return null;
    }

    // =========================
    // Relevance sort
    // =========================

    private void sortByRelevance(List<CarModel> list, String q) {
        if (list == null || list.size() <= 1) return;

        final String nq = normalizeForCompare(q);
        if (nq.isEmpty()) return;

        List<Scored<CarModel>> scored = new ArrayList<>(list.size());
        for (CarModel m : list) {
            scored.add(new Scored<>(m, relevanceScore(m, nq)));
        }
        scored.sort(
                Comparator.<Scored<CarModel>>comparingDouble(s -> s.score).reversed()
                        .thenComparing(s -> {
                            try {
                                return s.value.getModelId();
                            } catch (Exception e) {
                                return 0L;
                            }
                        }, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        list.clear();
        for (Scored<CarModel> s : scored) list.add(s.value);
    }

    private double relevanceScore(CarModel m, String nq) {
        if (m == null) return 0.0;

        String modelName = safe(m.getModelName());
        String brandName = (m.getBrand() != null) ? safe(m.getBrand().getBrandName()) : "";

        String nm = normalizeForCompare(modelName);
        String nb = normalizeForCompare(brandName);

        double score = 0.0;

        // Exact / prefix / contains
        if (!nm.isEmpty()) {
            if (nm.equals(nq)) score += 100;
            else if (nm.startsWith(nq)) score += 80;
            else if (nm.contains(nq)) score += 60;
            score += similarity(nq, nm) * 40;
        }
        if (!nb.isEmpty()) {
            if (nb.equals(nq)) score += 70;
            else if (nb.startsWith(nq)) score += 55;
            else if (nb.contains(nq)) score += 45;
            score += similarity(nq, nb) * 20;
        }

        // 동점 보정
        try {
            score += Math.min(10, (m.getViewCount() == null ? 0 : m.getViewCount()) / 1000.0);
            score += Math.min(10, (m.getLikeCount() == null ? 0 : m.getLikeCount()) / 1000.0);
        } catch (Exception ignored) {
        }

        return score;
    }

    private static class Scored<T> {
        final T value;
        final double score;

        Scored(T value, double score) {
            this.value = value;
            this.score = score;
        }
    }

    // =========================
    // Utils
    // =========================

    private String normalizeNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * 비교용 정규화: 공백/언더스코어/하이픈 제거 + 소문자
     */
    private String normalizeForCompare(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = t.replaceAll("[\\s_\\-]+", "");
        return t;
    }

    /**
     * 0~1 유사도 (1이 완전 동일)
     */
    private double similarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        if (a.equals(b)) return 1.0;
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        int dist = levenshtein(a, b);
        return 1.0 - (double) dist / (double) max;
    }

    /**
     * 레벤슈타인 거리 (O(n*m))
     */
    private int levenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        int n = s1.length();
        int m = s2.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;
                int del = prev[j] + 1;
                int ins = curr[j - 1] + 1;
                int sub = prev[j - 1] + cost;
                curr[j] = Math.min(Math.min(del, ins), sub);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[m];
    }
}
