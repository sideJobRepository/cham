package com.cham.collector.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 게시물 제목·파일명에서 몇 년 몇 월분인지 읽는다.
 *
 *   '2026년 8월 업무추진비 집행내역'        → 2026-08
 *   '2026.8월 중구의회 업무추진비 …'         → 2026-08
 *   '(26.8월분) 업무추진비 집행내역'         → 2026-08
 *   '업무추진비 공개 2608.xlsx'              → 2026-08
 *   '8월분 업무추진비' + 게시일 2026-09-02   → 2026-08 (게시일 기준. 게시월보다 뒤 달이면 작년)
 */
public final class PeriodParser {

    private static final Pattern YEAR_MONTH_KR = Pattern.compile("(20\\d{2})\\s*년\\s*(\\d{1,2})\\s*월");
    private static final Pattern YEAR_DOT_MONTH = Pattern.compile("(?<!\\d)(20\\d{2})\\s*[.\\-/_]\\s*(\\d{1,2})(?!\\d)");
    private static final Pattern SHORT_YEAR_DOT_MONTH = Pattern.compile("(?<!\\d)(\\d{2})\\s*\\.\\s*(\\d{1,2})\\s*월");
    private static final Pattern YYMM = Pattern.compile("(?<![\\d_])(\\d{2})(0[1-9]|1[0-2])(?![\\d])");
    private static final Pattern MONTH_ONLY = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*월");

    public static Optional<YearMonth> parse(String title, String filename, LocalDate postDate) {
        Optional<YearMonth> fromTitle = parseText(title, postDate);
        return fromTitle.isPresent() ? fromTitle : parseText(filename, postDate);
    }

    static Optional<YearMonth> parseText(String text, LocalDate postDate) {
        if (text == null || text.isBlank()) return Optional.empty();

        Matcher m = YEAR_MONTH_KR.matcher(text);
        if (m.find()) return of(m.group(1), m.group(2));

        m = YEAR_DOT_MONTH.matcher(text);
        if (m.find()) return of(m.group(1), m.group(2));

        m = SHORT_YEAR_DOT_MONTH.matcher(text);
        if (m.find()) return of("20" + m.group(1), m.group(2));

        m = YYMM.matcher(text);
        while (m.find()) {
            int year = 2000 + Integer.parseInt(m.group(1));
            if (year >= 2020 && year <= LocalDate.now().getYear() + 1) return of(String.valueOf(year), m.group(2));
        }

        m = MONTH_ONLY.matcher(text);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            if (month < 1 || month > 12) return Optional.empty();
            LocalDate base = postDate != null ? postDate : LocalDate.now();
            int year = month > base.getMonthValue() ? base.getYear() - 1 : base.getYear();
            return Optional.of(YearMonth.of(year, month));
        }
        return Optional.empty();
    }

    private static Optional<YearMonth> of(String year, String month) {
        int m = Integer.parseInt(month);
        if (m < 1 || m > 12) return Optional.empty();
        return Optional.of(YearMonth.of(Integer.parseInt(year), m));
    }

    private PeriodParser() {
    }
}
