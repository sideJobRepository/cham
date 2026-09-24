package com.cham.collect.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF 표 → 엑셀 시트. 만든 시트는 HeaderMappedSheetParser 가 엑셀과 똑같이 읽는다.
 *
 * 기관 PDF 는 한글·엑셀에서 PDF 로 저장한 것이라 글자가 텍스트로 들어 있다(스캔 아님).
 * 괘선을 따라 칸을 나누는 방식(Tabula lattice)으로 뽑는다. 줄 단위 텍스트로 읽으면
 * 두 줄에 걸친 칸이 어느 줄 값인지 알 수 없어서다.
 *
 * - 칸 수가 같은 표는 한 시트로 잇는다. 페이지를 넘어가는 표가 이렇게 이어진다.
 *   2페이지에 헤더가 다시 나오면 파서가 반복 헤더로 보고 건너뛴다
 * - 칸이 3개 미만인 표(제목 한 줄짜리)는 버린다
 * - 시트 이름은 'Sheet1' 류로 둔다. 파서가 시트 이름을 직함이나 비고로 쓰지 않게
 */
public final class PdfTableWorkbook {

    private static final int MIN_COLUMNS = 3;

    public static Workbook from(byte[] pdf) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        // 칸 수 → 시트. 같은 칸 수의 표는 같은 시트에 이어 붙인다
        Map<Integer, Sheet> sheetByColumns = new LinkedHashMap<>();

        try (PDDocument doc = PDDocument.load(pdf)) {
            ObjectExtractor extractor = new ObjectExtractor(doc);
            SpreadsheetExtractionAlgorithm lattice = new SpreadsheetExtractionAlgorithm();
            PageIterator pages = extractor.extract();
            while (pages.hasNext()) {
                Page page = pages.next();
                for (Table table : lattice.extract(page)) {
                    if (table.getColCount() < MIN_COLUMNS) continue;
                    Sheet sheet = sheetByColumns.computeIfAbsent(table.getColCount(),
                            cols -> workbook.createSheet("Sheet" + (sheetByColumns.size() + 1)));
                    append(sheet, table);
                }
            }
        }
        return workbook;
    }

    private static void append(Sheet sheet, Table table) {
        int rowIdx = sheet.getPhysicalNumberOfRows() == 0 ? 0 : sheet.getLastRowNum() + 1;
        for (List<RectangularTextContainer> cells : table.getRows()) {
            boolean empty = true;
            Row row = sheet.createRow(rowIdx);
            for (int c = 0; c < cells.size(); c++) {
                String text = clean(cells.get(c).getText());
                if (text == null) continue;
                row.createCell(c).setCellValue(text);
                empty = false;
            }
            if (empty) {
                sheet.removeRow(row);
            } else {
                rowIdx++;
            }
        }
    }

    // 칸 안 줄바꿈은 공백으로. '예결위 소관 …\r간담회' 처럼 긴 글이 여러 줄로 나온다
    private static String clean(String s) {
        if (s == null) return null;
        String v = s.replace('\r', ' ').replace('\n', ' ').replace(' ', ' ').replaceAll("\\s+", " ").trim();
        return v.isEmpty() ? null : v;
    }

    private PdfTableWorkbook() {
    }
}
