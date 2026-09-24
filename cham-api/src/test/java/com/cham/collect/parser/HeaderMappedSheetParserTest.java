package com.cham.collect.parser;

import com.cham.caruse.CardUseRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderMappedSheetParserTest {

    private final HeaderMappedSheetParser parser = new HeaderMappedSheetParser();

    private static final SourceDefaults JUNGGU_MAYOR = new SourceDefaults("기초지자체", "대전 중구", "구청장", "김제선", 2026, 8);
    private static final SourceDefaults JUNGGU_COUNCIL = new SourceDefaults("기초의회", "대전 중구", null, null, 2026, 8);

    @Test
    void 중구청형_시트_두개_제목줄_합계줄() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            for (String sheetName : List.of("기관운영", "시책추진")) {
                Sheet s = wb.createSheet(sheetName);
                row(s, 0, "구청장 업무추진비 사용내역(2026년 8월)");
                s.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
                row(s, 1, "", "", "", "", "", "", "", "(단위: 원)");
                row(s, 2, "연번", "사용일자", "시간", "사용자", "사용목적(내역)", "대상인원(명)", "사용금액(원)", "사용방법");
                row(s, 3, "1", "2026.08.03.", "12:30", "구청장", "위생과 전○○ 외조모상", "1", "100,000", "카드");
                row(s, 4, "2", "2026. 8. 14.", "18시 5분", "구청장", "간담회 식대", "6", "180000", "카드");
                row(s, 5, "계", "", "", "", "", "", "280,000", "");
            }

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_MAYOR);

            assertThat(result.sheets()).extracting(HeaderMappedSheetParser.SheetInfo::used).containsExactly(true, true);
            assertThat(result.sheets().get(0).headerRowNum()).isEqualTo(3);
            assertThat(result.rows()).hasSize(4);
            assertThat(result.blockingCount()).isZero();

            CardUseRow first = result.rows().get(0).row();
            assertThat(first.ownerPosition()).isEqualTo("기초지자체");
            assertThat(first.region()).isEqualTo("대전 중구");
            assertThat(first.user()).isEqualTo("구청장");
            assertThat(first.name()).isEqualTo("김제선");
            assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 3));
            assertThat(first.time()).isEqualTo(LocalTime.of(12, 30));
            assertThat(first.amount()).isEqualTo(100000.0);
            assertThat(first.personnel()).isEqualTo("1");
            assertThat(first.method()).isEqualTo("카드");
            assertThat(first.addrName()).isNull();
            assertThat(first.remark()).isEqualTo("기관운영");
            assertThat(result.rows().get(0).warnings()).contains("장소 없음", "주소 없음");

            CardUseRow second = result.rows().get(1).row();
            assertThat(second.date()).isEqualTo(LocalDate.of(2026, 8, 14));
            assertThat(second.time()).isEqualTo(LocalTime.of(18, 5));
            assertThat(result.rows().get(3).row().remark()).isEqualTo("시책추진");
        }
    }

    @Test
    void 중구의회형_사용일시_한칸_가맹점명_주소() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("⊙");
            row(s, 0, "연번", "사용일시", "사용자", "사용장소(가맹점명)", "가맹점 주소", "사용목적", "대상인원(명)", "사용금액(원)", "사용방법");
            row(s, 1, "1", "2026-08-03 12:30", "의장", "스시무희", "대전 중구 중앙로112번길 24", "간담회", "4명", "120,000", "카드");
            Row r2 = row(s, 2, "2", "", "국장(시책추진)", "칼국수집", "대전 중구 대종로 1", "직원 격려", "10", "", "현금");
            // 엑셀 날짜 서식 숫자 (2026-08-20 19:10)
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
            Cell dateCell = r2.createCell(1);
            dateCell.setCellValue(LocalDateTime.of(2026, 8, 20, 19, 10));
            dateCell.setCellStyle(dateStyle);
            r2.createCell(7).setCellValue(55000);

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_COUNCIL);

            assertThat(result.rows()).hasSize(2);
            CardUseRow first = result.rows().get(0).row();
            assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 3));
            assertThat(first.time()).isEqualTo(LocalTime.of(12, 30));
            assertThat(first.ownerPosition()).isEqualTo("기초의회");
            assertThat(first.user()).isEqualTo("의장");
            assertThat(first.name()).isNull();
            assertThat(first.addrName()).isEqualTo("스시무희");
            assertThat(first.addrDetail()).isEqualTo("대전 중구 중앙로112번길 24");
            assertThat(first.personnel()).isEqualTo("4명");
            // 시트가 하나면 비고에 시트명을 넣지 않는다
            assertThat(first.remark()).isNull();
            assertThat(result.rows().get(0).warnings()).isEmpty();

            CardUseRow second = result.rows().get(1).row();
            assertThat(second.date()).isEqualTo(LocalDate.of(2026, 8, 20));
            assertThat(second.time()).isEqualTo(LocalTime.of(19, 10));
            assertThat(second.user()).isEqualTo("국장(시책추진)");
            assertThat(second.amount()).isEqualTo(55000.0);
        }
    }

    @Test
    void 날짜를_못읽으면_막는다() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Sheet1");
            row(s, 0, "사용일자", "사용자", "사용목적", "사용금액");
            row(s, 1, "추후기재", "구청장", "간담회", "30000");
            row(s, 2, "2026-07-31", "구청장", "간담회", "30000");

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_MAYOR);

            assertThat(result.rows()).hasSize(2);
            assertThat(result.rows().get(0).blocking()).isTrue();
            assertThat(result.rows().get(0).warnings().get(0)).startsWith(HeaderMappedSheetParser.WARN_DATE);
            assertThat(result.rows().get(1).blocking()).isFalse();
            assertThat(result.rows().get(1).warnings()).contains("대상 월(2026-08)과 다른 날짜");
            assertThat(result.blockingCount()).isEqualTo(1);
        }
    }

    @Test
    void 헤더가_없는_시트는_쓰지_않는다() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet notes = wb.createSheet("안내");
            row(notes, 0, "개인정보 보호를 위해 이름은 가렸습니다");
            Sheet data = wb.createSheet("내역");
            row(data, 0, "집행일자", "집행목적", "집행금액", "결제방법");
            row(data, 1, "20260805", "행사 격려", "50000", "카드");

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_MAYOR);

            assertThat(result.sheets()).extracting(HeaderMappedSheetParser.SheetInfo::used).containsExactly(false, true);
            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().get(0).row().date()).isEqualTo(LocalDate.of(2026, 8, 5));
            assertThat(result.fileWarnings()).isEmpty();
        }
    }

    @Test
    void 헤더를_하나도_못찾으면_파일_경고() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("x");
            row(s, 0, "가", "나", "다");
            row(s, 1, "1", "2", "3");

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_MAYOR);

            assertThat(result.rows()).isEmpty();
            assertThat(result.fileWarnings()).hasSize(1);
        }
    }

    @Test
    void 동구의회형_사용자칸_없이_시트이름이_직함() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            for (String role : List.of("의장", "부의장")) {
                Sheet s = wb.createSheet(role);
                row(s, 0, role + " 업무추진비 집행내역");
                row(s, 1, "연번", "사용일자", "사용시간", "사용금액", "집행내역(목적)", "장 소", "인 원", "결제방법", "소재지");
                row(s, 2, "1", "2026-08-03", "12:32", "246000", "현안 논의", "정참치", "9", "카드", "동구 가오동");
            }

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_COUNCIL);

            assertThat(result.rows()).extracting(r -> r.row().user()).containsExactly("의장", "부의장");
            // 시트 이름을 사용자로 썼으니 비고에는 넣지 않는다
            assertThat(result.rows()).extracting(r -> r.row().remark()).containsOnlyNulls();
            assertThat(result.rows().get(0).row().addrName()).isEqualTo("정참치");
            assertThat(result.rows().get(0).row().addrDetail()).isEqualTo("동구 가오동");
        }
    }

    @Test
    void 서구청형_한시트에_표_둘_중간_제목줄과_헤더줄은_건너뛴다() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("Sheet1");
            row(s, 0, "2026년 8월 기관운영 업무추진비(구청장)");
            s.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            row(s, 1, "연번", "사용일시", "집행내역", "인원", "금 액", "상 호 명", "소 재 지", "결제방식");
            row(s, 2, "1", "2026-08-05 20:23", "직원 격려", "16", "295000", "어명", "서구 둔산동", "카드");
            row(s, 3, "2026년 8월 시책추진 업무추진비(구청장)");
            s.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));
            row(s, 4, "연번", "사용일시", "집행내역", "인원", "금 액", "상 호 명", "소 재 지", "결제방식");
            row(s, 5, "1", "2026-08-12 12:00", "간담회", "8", "160000", "칼국수", "서구 괴정동", "카드");

            HeaderMappedSheetParser.ParseResult result = parser.parse(wb, JUNGGU_MAYOR);

            assertThat(result.blockingCount()).isZero();
            assertThat(result.rows()).hasSize(2);
            assertThat(result.rows().get(0).row().method()).isEqualTo("카드");
            // 사용자 칸이 없고 시트 이름('Sheet1')도 직함이 아니면 기관 기본 직함
            assertThat(result.rows().get(0).row().user()).isEqualTo("구청장");
            assertThat(result.rows().get(1).row().amount()).isEqualTo(160000.0);
        }
    }

    @Test
    void 장소명_괄호안_주소를_나눈다() {
        assertThat(CollectHeaderRules.splitPlaceAndAddr("금강휴게소 (충북 옥천군 동이면 금강로 596)"))
                .containsExactly("금강휴게소", "충북 옥천군 동이면 금강로 596");
        assertThat(CollectHeaderRules.splitPlaceAndAddr("금광한정식 (중구 충무로 127)"))
                .containsExactly("금광한정식", "중구 충무로 127");
        assertThat(CollectHeaderRules.splitPlaceAndAddr("스타벅스(대전둔산점)")).isNull();
        assertThat(CollectHeaderRules.splitPlaceAndAddr("경조사비")).isNull();
    }

    @Test
    void 직함으로_볼_시트이름() {
        assertThat(CollectHeaderRules.isRoleSheetName("부의장")).isTrue();
        assertThat(CollectHeaderRules.isRoleSheetName("시책(국장)")).isTrue();
        assertThat(CollectHeaderRules.isRoleSheetName("Sheet1")).isFalse();
        assertThat(CollectHeaderRules.isRoleSheetName("업무추진비(구청장)_8월")).isFalse();
        assertThat(CollectHeaderRules.isRoleSheetName("⊙")).isFalse();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "2026.08.03.|2026-08-03",
            "2026. 8. 3.|2026-08-03",
            "2026-08-03|2026-08-03",
            "2026/8/3|2026-08-03",
            "2026년 8월 3일|2026-08-03",
            "20260803|2026-08-03",
            "08.03|2026-08-03",
            "8월 3일|2026-08-03",
    })
    void 날짜_표기(String raw, String expected) {
        assertThat(CollectCellParsers.dateFromText(raw, 2026)).isEqualTo(LocalDate.parse(expected));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12:30|12:30",
            "9:05|09:05",
            "18시 5분|18:05",
            "18시|18:00",
            "12:30~13:30|12:30",
    })
    void 시간_표기(String raw, String expected) {
        assertThat(CollectCellParsers.timeFromText(raw)).isEqualTo(LocalTime.parse(expected));
    }

    @Test
    void 헤더_정규화() {
        assertThat(CollectHeaderRules.match("사용금액(원)")).contains(CollectField.AMOUNT);
        assertThat(CollectHeaderRules.match("사용장소(가맹점명)")).contains(CollectField.ADDR_NAME);
        assertThat(CollectHeaderRules.match("가맹점 주소")).contains(CollectField.ADDR_DETAIL);
        assertThat(CollectHeaderRules.match("대상인원\n(명)")).contains(CollectField.PERSONNEL);
        assertThat(CollectHeaderRules.match("사용일시")).contains(CollectField.DATETIME);
        assertThat(CollectHeaderRules.match("사용일자")).contains(CollectField.DATE);
        assertThat(CollectHeaderRules.match("구분")).isEmpty();
    }

    private static Row row(Sheet sheet, int idx, String... values) {
        Row row = sheet.createRow(idx);
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && !values[i].isEmpty()) {
                row.createCell(i).setCellValue(values[i]);
            }
        }
        return row;
    }
}
