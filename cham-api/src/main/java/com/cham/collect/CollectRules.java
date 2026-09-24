package com.cham.collect;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 수집 공통 규칙. 수집기(cham-collector)의 collector.min-period 와 같은 값이어야 한다.
 */
public final class CollectRules {

    // 자동수집은 2026년 7월분부터. 그 전 달은 월 표에서도 흐리게 두고 요청을 받지 않는다
    public static final YearMonth START = YearMonth.of(2026, 7);

    // 민선 9기·지방의회 새 임기 시작(2026-06 지방선거). 이름 채우기는 이날 이후 자료에서만 찾는다.
    // 이전 임기 자료로 채우면 새 임기 자료에 전 의장·전 구청장 이름이 붙는다
    public static final LocalDate TERM_START = LocalDate.of(2026, 7, 1);

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
