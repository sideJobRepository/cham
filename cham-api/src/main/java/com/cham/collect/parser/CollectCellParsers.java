package com.cham.collect.parser;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 수집 원본 셀 읽기. 기존 PoiUtil 과 달리 못 읽으면 예외 대신 null 을 준다.
 * 미리보기에서 어느 줄이 왜 안 되는지 한꺼번에 보여줘야 해서다.
 */
final class CollectCellParsers {

    private static final DataFormatter FORMATTER = new DataFormatter();

    // 2026.08.03. / 2026. 8. 3. / 2026-08-03 12:30 / 2026년 8월 3일
    private static final Pattern FULL_DATE = Pattern.compile(
            "(20\\d{2}|19\\d{2})\\s*[.\\-/년]\\s*(\\d{1,2})\\s*[.\\-/월]\\s*(\\d{1,2})\\s*일?\\.?");
    // 20260803
    private static final Pattern COMPACT_DATE = Pattern.compile("(?<!\\d)(20\\d{2})(\\d{2})(\\d{2})(?!\\d)");
    // 08.03 / 8월 3일 (연도 없음 → 대상 연도)
    private static final Pattern MONTH_DAY = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*[.\\-/월]\\s*(\\d{1,2})\\s*일?(?!\\d)");
    // 12:30 / 12시30분 / 12시 30분
    private static final Pattern TIME = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*[:시]\\s*(\\d{1,2})");
    // 12시 (분 없음)
    private static final Pattern HOUR_ONLY = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*시(?!\\s*\\d)");

    /** 일자와 시간. 시간을 못 찾으면 time 은 null */
    record DateAndTime(LocalDate date, LocalTime time) {
    }

    /** 셀 → 화면에 보이는 문자열. 공백 정리 */
    static String text(Cell cell) {
        if (cell == null) return null;
        String v;
        try {
            v = switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case FORMULA -> cell.getCachedFormulaResultType() == CellType.STRING
                        ? cell.getStringCellValue()
                        : FORMATTER.formatCellValue(cell);
                case BLANK, ERROR, _NONE -> null;
                default -> FORMATTER.formatCellValue(cell);
            };
        } catch (RuntimeException e) {
            v = FORMATTER.formatCellValue(cell);
        }
        if (v == null) return null;
        v = v.replace(' ', ' ').replaceAll("\\s+", " ").trim();
        return v.isEmpty() ? null : v;
    }

    /** 일자. 연도 없는 '08.03' 은 defaultYear 로 채운다 */
    static LocalDate date(Cell cell, Integer defaultYear) {
        if (cell == null) return null;
        DateAndTime dt = fromNumeric(cell);
        if (dt != null) return dt.date();
        return dateFromText(text(cell), defaultYear);
    }

    static LocalDate dateFromText(String s, Integer defaultYear) {
        if (s == null) return null;
        Matcher m = FULL_DATE.matcher(s);
        if (m.find()) return safeDate(m.group(1), m.group(2), m.group(3));
        m = COMPACT_DATE.matcher(s);
        if (m.find()) return safeDate(m.group(1), m.group(2), m.group(3));
        if (defaultYear != null) {
            m = MONTH_DAY.matcher(s);
            if (m.find()) return safeDate(String.valueOf(defaultYear), m.group(1), m.group(2));
        }
        return null;
    }

    /** 시간. 못 읽으면 null (기존 업로드처럼 00:00 으로 메우지 않는다) */
    static LocalTime time(Cell cell) {
        if (cell == null) return null;
        if (numeric(cell)) {
            double v = cell.getNumericCellValue();
            double frac = v - Math.floor(v);
            if (v >= 0 && (v < 1 || frac > 0)) {
                return fractionToTime(frac);
            }
            return null;
        }
        return timeFromText(text(cell));
    }

    static LocalTime timeFromText(String s) {
        if (s == null) return null;
        Matcher m = TIME.matcher(s);
        if (m.find()) return safeTime(m.group(1), m.group(2));
        m = HOUR_ONLY.matcher(s);
        if (m.find()) return safeTime(m.group(1), "0");
        return null;
    }

    /** '사용일시' 한 칸 → 일자 + 시간 */
    static DateAndTime dateTime(Cell cell, Integer defaultYear) {
        if (cell == null) return null;
        DateAndTime dt = fromNumeric(cell);
        if (dt != null) return dt;
        String s = text(cell);
        LocalDate d = dateFromText(s, defaultYear);
        if (d == null) return null;
        // 날짜 부분을 지운 뒤에 시간을 찾는다. '2026.08.03' 의 '08.03' 을 시간으로 읽지 않게
        String rest = FULL_DATE.matcher(s).replaceFirst(" ");
        rest = COMPACT_DATE.matcher(rest).replaceFirst(" ");
        return new DateAndTime(d, timeFromText(rest));
    }

    static Double amount(Cell cell) {
        if (cell == null) return null;
        if (numeric(cell)) return cell.getNumericCellValue();
        String s = text(cell);
        if (s == null) return null;
        String digits = s.replaceAll("[^0-9.\\-]", "");
        if (digits.isEmpty() || digits.equals("-") || digits.equals(".")) return null;
        try {
            return Double.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String personnel(Cell cell) {
        if (cell == null) return null;
        if (numeric(cell)) return String.valueOf((int) cell.getNumericCellValue());
        return text(cell);
    }

    private static boolean numeric(Cell cell) {
        return cell.getCellType() == CellType.NUMERIC
                || (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC);
    }

    private static DateAndTime fromNumeric(Cell cell) {
        if (!numeric(cell)) return null;
        double v = cell.getNumericCellValue();
        if (DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate(v) && v >= 1) {
            return fromSerial(v);
        }
        // 20260803 을 숫자로 적은 경우
        long l = (long) v;
        if (v == l && l >= 19000101L && l <= 21001231L) {
            LocalDate d = safeDate(String.valueOf(l / 10000), String.valueOf(l / 100 % 100), String.valueOf(l % 100));
            return d == null ? null : new DateAndTime(d, null);
        }
        // 날짜 서식이 빠진 엑셀 날짜 일련번호 (2000~2059 년 범위만)
        if (v >= 36526 && v < 58440) {
            return fromSerial(v);
        }
        return null;
    }

    // 소수부가 없으면 시간은 적혀 있지 않은 것으로 본다(자정으로 읽지 않는다)
    private static DateAndTime fromSerial(double v) {
        LocalDateTime dt = DateUtil.getLocalDateTime(v);
        LocalTime t = (v == Math.floor(v)) ? null : fractionToTime(v - Math.floor(v));
        return new DateAndTime(dt.toLocalDate(), t);
    }

    private static LocalTime fractionToTime(double frac) {
        int minutes = (int) Math.round(frac * 24 * 60) % (24 * 60);
        return LocalTime.of(minutes / 60, minutes % 60);
    }

    private static LocalDate safeDate(String y, String m, String d) {
        try {
            return LocalDate.of(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LocalTime safeTime(String h, String m) {
        try {
            int hh = Integer.parseInt(h);
            int mm = Integer.parseInt(m);
            if (hh == 24 && mm == 0) return LocalTime.MIDNIGHT;
            return LocalTime.of(hh, mm);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private CollectCellParsers() {
    }
}
