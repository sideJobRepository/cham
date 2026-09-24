package com.cham.caruse;

import com.cham.advice.exception.ExcelException;
import com.cham.util.ExcelColumns;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 수동 업로드(/cham/upload) 양식 엑셀 만들기.
 *
 * 열 순서는 업로드가 읽는 ExcelColumns 를 그대로 따른다. 받은 파일을 고쳐서 다시 올릴 수 있어야 한다.
 * 공개관리의 '엑셀' 내려받기와 수집관리의 '업로드 양식' 내려받기가 같이 쓴다.
 */
public final class UploadFormExcel {

    public static final String[] HEADERS = {
            "기관/직책", "지역", "사용자", "이름", "집행일자", "시간", "사용장소명",
            "상세주소", "집행목적", "대상인원", "금액", "결제방법", "비고", "삭제키"
    };

    public static byte[] write(List<CardUseRow> rows, String deleteKey) {
        // 행이 많아질 수 있어 SXSSF 로 쓴다. 100행만 메모리에 두고 나머지는 디스크로 흘린다.
        // 디스크로 흘린 임시파일은 close() 가 같이 지운다(POI 5.4 부터 dispose 는 없어졌다).
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("업무추진비");

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowNum = 1;
            for (CardUseRow use : rows) {
                Row row = sheet.createRow(rowNum++);

                setText(row, ExcelColumns.OWNER_POSITION, use.ownerPosition());
                setText(row, ExcelColumns.REGION, use.region());
                setText(row, ExcelColumns.USER_SELL, use.user());
                setText(row, ExcelColumns.NAME_SELL, use.name());

                // 날짜·시간은 문자열로 쓴다. 업로드 쪽 파서가 이 형식(yyyy-MM-dd / HH:mm)을 그대로 읽는다.
                setText(row, ExcelColumns.DATE, use.date() == null ? null : use.date().toString());
                setText(row, ExcelColumns.TIME, use.time() == null ? null : use.time().toString());

                setText(row, ExcelColumns.ADDR_NAME, use.addrName());
                setText(row, ExcelColumns.ADDR_DETAIL, use.addrDetail());
                setText(row, ExcelColumns.PURPOSE, use.purpose());
                setText(row, ExcelColumns.PERSONNEL, use.personnel());

                if (use.amount() == null) {
                    setText(row, ExcelColumns.AMOUNT, null);
                } else {
                    row.createCell(ExcelColumns.AMOUNT).setCellValue(use.amount());
                }

                setText(row, ExcelColumns.METHOD, use.method());
                setText(row, ExcelColumns.REMARK, use.remark());
                setText(row, ExcelColumns.DELKEY, deleteKey);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new ExcelException("엑셀을 만드는 중 오류가 발생했습니다.", 400);
        }
    }

    // 빈 값도 빈 문자열로 채운다. 칸을 아예 비워두면 다시 올릴 때 null 이 되어 터지는 자리가 있다.
    private static void setText(Row row, int column, String value) {
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    private UploadFormExcel() {
    }
}
