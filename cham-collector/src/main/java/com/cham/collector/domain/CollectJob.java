package com.cham.collector.domain;

import java.time.YearMonth;

/** CHAM_MONIMAP_COLLECT_JOB 한 줄 (수집기가 읽는 칸만) */
public record CollectJob(
        Long id,
        String trigger,     // SCHEDULED / MANUAL
        Long sourceId,      // null 이면 사용 중인 기관 전체
        Integer pageLimit,
        Integer targetYear,
        Integer targetMonth
) {

    public boolean scheduled() {
        return "SCHEDULED".equals(trigger);
    }

    public YearMonth target() {
        return targetYear == null || targetMonth == null ? null : YearMonth.of(targetYear, targetMonth);
    }
}
