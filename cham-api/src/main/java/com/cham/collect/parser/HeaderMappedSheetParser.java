package com.cham.collect.parser;

import com.cham.caruse.CardUseRow;
import com.cham.caruse.CleanupRules;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * 기관이 올린 원본 엑셀을 헤더 이름으로 읽어 CardUseRow 로 바꾼다.
 *
 * 수동 업로드(ExcelColumns)는 열 위치가 고정이라 사람이 14열로 맞춰야 했다.
 * 여기서는 시트마다 위쪽에서 헤더 줄을 찾아 CollectHeaderRules 사전으로 칸을 정한다.
 * 시트 이름은 보지 않는다(중구의회는 시트명이 '⊙' 하나다).
 *
 * 자리값(CardUseDefaults)은 넣지 않는다. 미리보기에 원본 그대로 보여주고 저장할 때 채운다.
 * Spring 에 기대지 않아서 new 로 만들어 바로 테스트한다.
 */
public class HeaderMappedSheetParser {

    public static final String WARN_DATE = "날짜를 읽지 못함";
    public static final String WARN_NO_PLACE = "장소 없음";
    public static final String WARN_NO_ADDR = "주소 없음";
    // 헤더를 못 찾은 파일. 예산 합계표·안내문처럼 건별 사용내역이 없는 파일이 대부분이다
    public static final String NO_TABLE = "사용내역 표가 없는 파일입니다(예산 합계표·안내문 등). '반영 안 함'으로 표시하세요.";

    public record ParseResult(List<ParsedRow> rows, List<SheetInfo> sheets, List<String> fileWarnings) {
        public long blockingCount() {
            return rows.stream().filter(ParsedRow::blocking).count();
        }

        public long warningCount() {
            return rows.stream().filter(r -> !r.warnings().isEmpty()).count();
        }
    }

    /**
     * @param mapping  원본 헤더 → 필드 이름 (화면 표시용)
     * @param unmapped 사전에 없어 버린 헤더. 새 단어를 CollectHeaderRules 에 더할 때 본다
     */
    public record SheetInfo(String name, boolean used, Integer headerRowNum, Map<String, String> mapping,
                            List<String> unmapped, int rowCount) {
    }

    /**
     * @param blocking 이면 이 파일은 반영할 수 없다 (날짜 없는 줄)
     * @param district 원본의 '구별' 칸(서구). 주소가 없을 때 장소 검색 범위로 쓴다
     */
    public record ParsedRow(CardUseRow row, List<String> warnings, boolean blocking, String district) {
        public ParsedRow(CardUseRow row, List<String> warnings, boolean blocking) {
            this(row, warnings, blocking, null);
        }
    }

    public ParseResult parse(Workbook workbook, SourceDefaults defaults) {
        List<SheetInfo> sheets = new ArrayList<>();
        List<ParsedRow> rows = new ArrayList<>();
        List<String> fileWarnings = new ArrayList<>();

        List<Sheet> visible = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.isSheetHidden(i) || workbook.isSheetVeryHidden(i)) continue;
            visible.add(workbook.getSheetAt(i));
        }

        List<SheetParse> parsed = new ArrayList<>();
        for (Sheet sheet : visible) {
            parsed.add(parseSheet(sheet, defaults));
        }
        long usedSheets = parsed.stream().filter(p -> p.info.used()).count();

        for (SheetParse p : parsed) {
            sheets.add(p.info);
            for (ParsedRow r : p.rows) {
                // 시트가 둘 이상이면(중구청 기관운영/시책추진) 어느 시트 줄인지 비고에 남긴다.
                // 시트 이름을 이미 사용자(직함)로 썼으면(동구의회 의장/부의장) 비고에 또 넣지 않는다
                if (usedSheets > 1 && !p.userFromSheetName && isBlank(r.row().remark())
                        && !CollectHeaderRules.isGenericSheetName(p.info.name())) {
                    r = new ParsedRow(withRemark(r.row(), p.info.name()), r.warnings(), r.blocking(), r.district());
                }
                rows.add(r);
            }
        }

        if (usedSheets == 0) {
            fileWarnings.add(NO_TABLE);
        } else if (rows.isEmpty()) {
            fileWarnings.add("헤더는 찾았지만 읽을 줄이 없습니다.");
        }
        return new ParseResult(rows, sheets, fileWarnings);
    }

    // ── 시트 하나 ──────────────────────────────────────────────

    private record SheetParse(SheetInfo info, List<ParsedRow> rows, boolean userFromSheetName) {
    }

    private SheetParse parseSheet(Sheet sheet, SourceDefaults defaults) {
        MergedLookup merged = new MergedLookup(sheet);

        int headerRow = -1;
        Map<CollectField, Integer> columns = null;
        Map<String, String> mapping = null;
        List<String> unmapped = null;

        int scanEnd = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + CollectHeaderRules.HEADER_SCAN_ROWS);
        for (int r = Math.max(0, sheet.getFirstRowNum()); r <= scanEnd; r++) {
            HeaderMatch hm = matchHeader(sheet, r, merged);
            if (hm.columns.size() >= CollectHeaderRules.MIN_HEADER_HITS) {
                headerRow = r;
                columns = hm.columns;
                mapping = hm.mapping;
                unmapped = hm.unmapped;
                break;
            }
        }
        if (headerRow < 0) {
            return new SheetParse(new SheetInfo(sheet.getSheetName(), false, null, Map.of(), List.of(), 0), List.of(), false);
        }

        // 2단 헤더: 바로 아래 줄에도 헤더 단어가 2개 이상 있고 날짜가 없으면 합친다
        int dataStart = headerRow + 1;
        HeaderMatch second = matchHeader(sheet, headerRow + 1, merged);
        if (!second.columns.isEmpty() && second.columns.size() >= 2 && !rowHasDate(sheet.getRow(headerRow + 1), defaults)) {
            for (Map.Entry<CollectField, Integer> e : second.columns.entrySet()) {
                if (!columns.containsKey(e.getKey()) && !columns.containsValue(e.getValue())) {
                    columns.put(e.getKey(), e.getValue());
                }
            }
            mapping.putAll(second.mapping);
            dataStart = headerRow + 2;
        }

        // 사용자(직함) 칸이 없으면 시트 이름이 직함인지 본다. 아니면 기관 기본 직함
        boolean hasUserColumn = columns.containsKey(CollectField.USER) || columns.containsKey(CollectField.POSITION);
        boolean userFromSheetName = !hasUserColumn && CollectHeaderRules.isRoleSheetName(sheet.getSheetName());
        String fallbackUser = userFromSheetName ? sheet.getSheetName().trim() : defaults.user();

        List<ParsedRow> rows = new ArrayList<>();
        for (int r = dataStart; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || isEmpty(row)) continue;
            if (isTotalRow(row, columns)) continue;
            // 한 시트에 표가 여럿이면(서구청 기관운영/시책추진) 중간에 제목 줄과 헤더 줄이 다시 나온다
            if (isTitleRow(sheet, row, columns, merged)) continue;
            if (matchHeader(sheet, r, merged).columns.size() >= CollectHeaderRules.MIN_HEADER_HITS) continue;

            ParsedRow parsed = readRow(sheet, row, columns, merged, defaults, fallbackUser);
            if (parsed != null) rows.add(parsed);
        }

        return new SheetParse(
                new SheetInfo(sheet.getSheetName(), true, headerRow + 1, mapping, unmapped, rows.size()),
                rows, userFromSheetName);
    }

    private record HeaderMatch(Map<CollectField, Integer> columns, Map<String, String> mapping, List<String> unmapped) {
    }

    private HeaderMatch matchHeader(Sheet sheet, int rowIdx, MergedLookup merged) {
        Map<CollectField, Integer> columns = new EnumMap<>(CollectField.class);
        Map<String, String> mapping = new LinkedHashMap<>();
        List<String> unmapped = new ArrayList<>();
        Row row = sheet.getRow(rowIdx);
        if (row == null) return new HeaderMatch(columns, mapping, unmapped);

        for (int c = Math.max(0, row.getFirstCellNum()); c < row.getLastCellNum(); c++) {
            String text = CollectCellParsers.text(merged.cell(sheet, rowIdx, c));
            if (text == null) continue;
            Optional<CollectField> field = CollectHeaderRules.match(text);
            if (field.isPresent() && !columns.containsKey(field.get())) {
                columns.put(field.get(), c);
                mapping.put(text, field.get().name());
            } else if (field.isEmpty()) {
                unmapped.add(text);
            }
        }
        return new HeaderMatch(columns, mapping, unmapped);
    }

    private ParsedRow readRow(Sheet sheet, Row row, Map<CollectField, Integer> columns,
                              MergedLookup merged, SourceDefaults defaults, String fallbackUser) {
        Integer year = defaults.targetYear();
        List<String> warnings = new ArrayList<>();

        LocalDate date = null;
        LocalTime time = null;
        String rawDate = null;
        if (columns.containsKey(CollectField.DATETIME)) {
            Cell cell = cellOf(sheet, row, columns, CollectField.DATETIME, merged);
            rawDate = CollectCellParsers.text(cell);
            CollectCellParsers.DateAndTime dt = CollectCellParsers.dateTime(cell, year);
            if (dt != null) {
                date = dt.date();
                time = dt.time();
            }
        }
        if (date == null && columns.containsKey(CollectField.DATE)) {
            Cell cell = cellOf(sheet, row, columns, CollectField.DATE, merged);
            rawDate = CollectCellParsers.text(cell);
            date = CollectCellParsers.date(cell, year);
        }
        if (time == null && columns.containsKey(CollectField.TIME)) {
            time = CollectCellParsers.time(cellOf(sheet, row, columns, CollectField.TIME, merged));
        }

        Double amount = CollectCellParsers.amount(cellOf(sheet, row, columns, CollectField.AMOUNT, merged));

        // 날짜도 금액도 없는 줄은 표 아래 메모('※ 개인정보 비공개' 등)로 보고 버린다
        if (date == null && isBlank(rawDate) && amount == null) return null;

        String user = str(sheet, row, columns, CollectField.USER, merged);
        String position = str(sheet, row, columns, CollectField.POSITION, merged);
        if (isBlank(user) && !isBlank(position)) user = position;
        if (isBlank(user)) user = fallbackUser;

        String name = str(sheet, row, columns, CollectField.NAME, merged);
        if (isBlank(name)) name = defaults.name();

        String addrName = str(sheet, row, columns, CollectField.ADDR_NAME, merged);
        String addrDetail = str(sheet, row, columns, CollectField.ADDR_DETAIL, merged);
        if (isBlank(addrDetail)) {
            String[] split = CollectHeaderRules.splitPlaceAndAddr(addrName);
            if (split != null) {
                addrName = split[0];
                addrDetail = split[1];
            }
        }

        boolean blocking = false;
        if (date == null) {
            warnings.add(WARN_DATE + (isBlank(rawDate) ? " (빈 칸)" : " '" + rawDate + "'"));
            blocking = true;
        } else if (year != null && defaults.targetMonth() != null
                && (date.getYear() != year || date.getMonthValue() != defaults.targetMonth())) {
            warnings.add(String.format("대상 월(%d-%02d)과 다른 날짜", year, defaults.targetMonth()));
        }
        if (time == null) warnings.add("시간 없음");
        if (isBlank(addrName)) warnings.add(WARN_NO_PLACE);
        else if (CleanupRules.isAddrNameSuspicious(addrName)) warnings.add("장소명 확인 필요");
        if (isBlank(addrDetail)) warnings.add(WARN_NO_ADDR);
        if (amount == null || amount == 0) warnings.add("금액 없음");

        CardUseRow cardUseRow = new CardUseRow(
                defaults.position(),
                defaults.region(),
                user,
                name,
                date,
                time,
                addrName,
                addrDetail,
                str(sheet, row, columns, CollectField.PURPOSE, merged),
                CollectCellParsers.personnel(cellOf(sheet, row, columns, CollectField.PERSONNEL, merged)),
                amount,
                str(sheet, row, columns, CollectField.METHOD, merged),
                str(sheet, row, columns, CollectField.REMARK, merged),
                row.getRowNum() + 1,
                sheet.getSheetName());
        return new ParsedRow(cardUseRow, warnings, blocking, str(sheet, row, columns, CollectField.DISTRICT, merged));
    }

    // ── 도우미 ────────────────────────────────────────────────

    private boolean isTotalRow(Row row, Map<CollectField, Integer> columns) {
        // 연번 칸이나 줄의 첫 글자 칸이 '합계' 류면 합계 줄
        Integer seq = columns.get(CollectField.SEQ);
        if (seq != null && CollectHeaderRules.isTotalWord(CollectCellParsers.text(row.getCell(seq)))) return true;
        for (Cell cell : row) {
            String t = CollectCellParsers.text(cell);
            if (t == null) continue;
            return CollectHeaderRules.isTotalWord(t);
        }
        return false;
    }

    /**
     * 표 제목 줄: 여러 칸에 걸친 병합 셀 하나라, 매핑된 칸을 읽으면 전부 같은 글자가 나온다.
     * 그대로 두면 '2026년 8월 시책추진 업무추진비' 의 숫자가 금액으로 읽힌다
     */
    private boolean isTitleRow(Sheet sheet, Row row, Map<CollectField, Integer> columns, MergedLookup merged) {
        Set<String> distinct = new HashSet<>();
        int filled = 0;
        for (Integer col : columns.values()) {
            String t = CollectCellParsers.text(merged.cell(sheet, row.getRowNum(), col));
            if (t == null) continue;
            filled++;
            distinct.add(t);
        }
        return filled >= 3 && distinct.size() == 1;
    }

    private boolean rowHasDate(Row row, SourceDefaults defaults) {
        if (row == null) return false;
        for (Cell cell : row) {
            if (CollectCellParsers.date(cell, null) != null) return true;
        }
        return false;
    }

    private boolean isEmpty(Row row) {
        for (Cell cell : row) {
            if (CollectCellParsers.text(cell) != null) return false;
        }
        return true;
    }

    private Cell cellOf(Sheet sheet, Row row, Map<CollectField, Integer> columns, CollectField field, MergedLookup merged) {
        Integer col = columns.get(field);
        if (col == null) return null;
        return merged.cell(sheet, row.getRowNum(), col);
    }

    private String str(Sheet sheet, Row row, Map<CollectField, Integer> columns, CollectField field, MergedLookup merged) {
        return CollectCellParsers.text(cellOf(sheet, row, columns, field, merged));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static CardUseRow withRemark(CardUseRow r, String remark) {
        return new CardUseRow(r.ownerPosition(), r.region(), r.user(), r.name(), r.date(), r.time(),
                r.addrName(), r.addrDetail(), r.purpose(), r.personnel(), r.amount(), r.method(), remark,
                r.sourceRowNum(), r.sheetName());
    }

    /** 병합 셀은 왼쪽 위 칸에만 값이 있다. 병합 범위 안의 칸을 물으면 왼쪽 위 칸을 준다 */
    private static final class MergedLookup {
        private final List<CellRangeAddress> regions;

        MergedLookup(Sheet sheet) {
            this.regions = sheet.getMergedRegions();
        }

        Cell cell(Sheet sheet, int rowIdx, int colIdx) {
            for (CellRangeAddress region : regions) {
                if (region.isInRange(rowIdx, colIdx)) {
                    Row top = sheet.getRow(region.getFirstRow());
                    return top == null ? null : top.getCell(region.getFirstColumn());
                }
            }
            Row row = sheet.getRow(rowIdx);
            return row == null ? null : row.getCell(colIdx);
        }
    }
}
