package com.cham.collect;

import java.time.YearMonth;

/**
 * 수집 공통 규칙. 수집기(cham-collector)의 collector.min-period 와 같은 값이어야 한다.
 */
public final class CollectRules {

    // 자동수집은 2026년 7월분부터. 그 전 달은 월 표에서도 흐리게 두고 요청을 받지 않는다
    public static final YearMonth START = YearMonth.of(2026, 7);

    public static boolean isCollectable(int year, int month) {
        return !YearMonth.of(year, month).isBefore(START) && !YearMonth.of(year, month).isAfter(YearMonth.now());
    }

    // 반영할 때 자동으로 붙이는 삭제키. 겹치면 -2, -3 을 붙인다
    public static String deleteKey(String sourceName, Integer year, Integer month) {
        if (year == null || month == null) return "수집-" + sourceName;
        return String.format("수집-%s-%d-%02d", sourceName, year, month);
    }

    private CollectRules() {
    }
}
