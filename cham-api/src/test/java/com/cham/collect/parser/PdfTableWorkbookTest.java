package com.cham.collect.parser;

import com.cham.caruse.CardUseRow;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2026-09-24 에 수집한 실제 PDF(2026년 8월분)로 표 추출 → 헤더 파서까지 확인한다.
 * 기관이 PDF 서식을 바꾸면 운영 미리보기에서 '헤더를 찾지 못함' 이나 경고로 먼저 드러난다.
 */
class PdfTableWorkbookTest {

    private final HeaderMappedSheetParser parser = new HeaderMappedSheetParser();

    @Test
    void 대전시_시장_3페이지_표가_한_시트로_이어진다() throws Exception {
        HeaderMappedSheetParser.ParseResult r = parse("daejeon-mayor-2608.pdf",
                new SourceDefaults("광역지자체", "대전", "시장", "이장우", 2026, 8));

        assertThat(r.blockingCount()).isZero();
        assertThat(r.rows()).hasSize(56);
        CardUseRow first = r.rows().get(0).row();
        assertThat(first.user()).isEqualTo("행정자치국장");
        assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(first.time()).isEqualTo(LocalTime.of(12, 53));
        assertThat(first.addrName()).isEqualTo("미향일식");
        assertThat(first.amount()).isEqualTo(90000.0);
        assertThat(first.personnel()).isEqualTo("5");
        assertThat(first.method()).isEqualTo("카드");
        // 시트가 하나라 비고는 원본 '비목' 값 그대로
        assertThat(first.remark()).isEqualTo("시책");
    }

    @Test
    void 대전시_요약표만_있는_파일은_내역이_없다() throws Exception {
        HeaderMappedSheetParser.ParseResult r = parse("daejeon-summary-2608.pdf",
                new SourceDefaults("광역지자체", "대전", "시장", "이장우", 2026, 8));

        assertThat(r.rows()).isEmpty();
        assertThat(r.fileWarnings()).isNotEmpty();
    }

    @Test
    void 대덕구청_장소칸의_괄호_주소를_나눈다() throws Exception {
        HeaderMappedSheetParser.ParseResult r = parse("daedeok-mayor-2608.pdf",
                new SourceDefaults("기초지자체", "대전 대덕구", "구청장", "최충규", 2026, 8));

        assertThat(r.blockingCount()).isZero();
        assertThat(r.rows()).hasSize(30);
        CardUseRow first = r.rows().get(0).row();
        assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(first.time()).isEqualTo(LocalTime.of(8, 33));
        assertThat(first.addrName()).isEqualTo("스타벅스");
        assertThat(first.addrDetail()).isEqualTo("대덕구 대덕대로 1544");
        assertThat(first.amount()).isEqualTo(34200.0);
        assertThat(first.user()).isEqualTo("구청장");
    }

    @Test
    void 유성구청_월일만_있는_날짜() throws Exception {
        HeaderMappedSheetParser.ParseResult r = parse("yuseong-mayor-2608.pdf",
                new SourceDefaults("기초지자체", "대전 유성구", "구청장", "정용래", 2026, 8));

        assertThat(r.blockingCount()).isZero();
        assertThat(r.rows()).hasSize(10);
        CardUseRow first = r.rows().get(0).row();
        assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 4));
        assertThat(first.amount()).isEqualTo(50000.0);
        CardUseRow second = r.rows().get(1).row();
        assertThat(second.addrName()).isEqualTo("칠리프");
        assertThat(second.addrDetail()).isEqualTo("어은로");
        assertThat(second.method()).isEqualTo("카드");
    }

    @Test
    void 대전시의회_2페이지로_넘어가는_표() throws Exception {
        HeaderMappedSheetParser.ParseResult r = parse("daejeon-council-2608.pdf",
                new SourceDefaults("광역의회", "대전", null, null, 2026, 8));

        assertThat(r.blockingCount()).isZero();
        assertThat(r.rows()).hasSize(27);
        List<HeaderMappedSheetParser.ParsedRow> rows = r.rows();
        CardUseRow last = rows.get(rows.size() - 1).row();
        assertThat(last.date()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(last.amount()).isEqualTo(41000.0);
        // '결재방법' 헤더도 결제방법으로 읽는다
        assertThat(last.method()).isEqualTo("신용카드");
    }

    private HeaderMappedSheetParser.ParseResult parse(String name, SourceDefaults defaults) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/collect-pdf/" + name);
             Workbook wb = PdfTableWorkbook.from(in.readAllBytes())) {
            return parser.parse(wb, defaults);
        }
    }
}
