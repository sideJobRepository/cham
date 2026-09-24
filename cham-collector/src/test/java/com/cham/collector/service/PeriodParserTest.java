package com.cham.collector.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodParserTest {

    // 2026-09-24 에 실제 목록에서 본 제목들
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "2026년 8월 업무추진비 집행내역|2026-08",
            "구청장 업무추진비 사용내역(2026년 8월)|2026-08",
            "2026년 8월중 구청장 업무추진비 집행내역|2026-08",
            "2026.8월 중구의회 업무추진비 사용내역 공개|2026-08",
            "2026년 8월 구청장 업무추진비 집행내역 공개|2026-08",
            "(26.8월분) 업무추진비 집행내역(홈페이지)_구청장.pdf|2026-08",
            "업무추진비 공개 2608.xlsx|2026-08",
            "2026-12 업무추진비|2026-12",
    })
    void 제목(String title, String expected) {
        assertThat(PeriodParser.parse(title, null, null)).contains(YearMonth.parse(expected));
    }

    @Test
    void 월만_있으면_게시일로_연도를_정한다() {
        assertThat(PeriodParser.parse("8월분 업무추진비", null, LocalDate.of(2026, 9, 2))).contains(YearMonth.of(2026, 8));
        assertThat(PeriodParser.parse("12월분 업무추진비", null, LocalDate.of(2027, 1, 5))).contains(YearMonth.of(2026, 12));
    }

    @Test
    void 제목에_없으면_파일명을_본다() {
        assertThat(PeriodParser.parse("업무추진비 사용내역 공개", "업무추진비 공개 2607.xlsx", null)).contains(YearMonth.of(2026, 7));
    }

    @Test
    void 못읽으면_비어있다() {
        assertThat(PeriodParser.parse("업무추진비 사용내역 공개", null, null)).isEmpty();
        assertThat(PeriodParser.parse("제9대 의회 업무추진비", null, null)).isEmpty();
    }
}
