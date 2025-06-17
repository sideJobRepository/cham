package com.cham.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalTime;

public class PoiUtil {
    
    
    public static String getCellValue(Row row, int cellNum) {
        if (row == null) return "";
        Cell cell = row.getCell(cellNum);
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                yield switch (cell.getCachedFormulaResultType()) {
                    case STRING -> cell.getStringCellValue();
                    case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                    default -> "";
                };
            }
            default -> "";
        };
    }
    public static LocalTime getLocalTimeFromCell(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return LocalTime.parse(cell.getStringCellValue()); // "12:30" 같은 텍스트
            case NUMERIC:
                double value = cell.getNumericCellValue();         // 0.52 같이 실수
                return LocalTime.ofSecondOfDay((int) (value * 86400)); // 하루 86400초
            default:
                throw new IllegalArgumentException("지원하지 않는 셀 타입입니다: " + cell.getCellType());
        }
    }
}
