package com.carproject.car.dto;

/**
 * 맞춤견적 설문: 예산 구간
 * - 단위: 원(￦)
 */
public enum BudgetRange {
    UNDER_2000(0L, 20_000_000L),
    BETWEEN_2000_3000(20_000_000L, 30_000_000L),
    BETWEEN_3000_4000(30_000_000L, 40_000_000L),
    BETWEEN_4000_5000(40_000_000L, 50_000_000L),
    BETWEEN_5000_7000(50_000_000L, 70_000_000L),
    OVER_7000(70_000_000L, Long.MAX_VALUE);

    private final long min;
    private final long max;

    BudgetRange(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public boolean contains(long priceWon) {
        return priceWon >= min && priceWon <= max;
    }
}
