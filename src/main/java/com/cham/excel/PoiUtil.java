package com.cham.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PoiUtil {
    public static String getCellValue(Row row, int cellNum) {
        if (row == null) return "";
        Cell cell = row.getCell(cellNum);
        if (cell == null) return "";
        
        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> switch (cell.getCachedFormulaResultType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
            default -> "";
        };
        
        return sanitize(value);
    }
    
    private static String sanitize(String input) {
        if (input == null) return "";
        return input
                .replace("\u00A0", " ")     // non-breaking space → 일반 공백
                .replaceAll("\\s+", " ")    // 연속 공백 → 공백 하나
                .trim();                    // 앞뒤 공백 제거
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
    
    public static LocalDate getLocalDateFromCell(Cell cell) {
        if (cell == null) return null;
        
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return DateUtil.getLocalDateTime(cell.getNumericCellValue()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            throw new IllegalStateException("Invalid cell type for date: " + cell.getCellType());
        }
    }
}
