package com.cham.util;

import com.cham.advice.exception.ExcelException;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PoiUtil {
    
    
    public static String getString(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> new DataFormatter().formatCellValue(c); // "1234"나 "1,234"도 문자열로
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(c);
            default -> null;
        };
    }
    
    
    public static Double getNumeric(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return null;
        return switch (c.getCellType()) {
            case NUMERIC -> c.getNumericCellValue();
            case STRING -> {
                String s = c.getStringCellValue();
                if (s == null) yield null;
                String cleaned = s.replaceAll("[,\\s]", "");
                try { yield Double.valueOf(cleaned); } catch (NumberFormatException e) { yield null; }
            }
            case FORMULA -> {
                if (c.getCachedFormulaResultType() == CellType.NUMERIC) yield c.getNumericCellValue();
                String fmt = new DataFormatter().formatCellValue(c);
                try { yield Double.valueOf(fmt.replaceAll("[,\\s]", "")); } catch (Exception e) { yield null; }
            }
            default -> null;
        };
    }
    
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
                throw new ExcelException("셀에 빈값이 있는지 확인해 주세요", 400);
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
            throw new ExcelException("날짜 형식이 올바르지 않습니다 : " + cell.getStringCellValue(), 400);
        }
    }
    
    public static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                    return false;
                }
                if (cell.getCellType() != CellType.STRING) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static String parsePersonnel(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> sanitize(c.getStringCellValue());
            case NUMERIC -> String.valueOf((int) c.getNumericCellValue());
            case FORMULA -> switch (c.getCachedFormulaResultType()) {
                case STRING  -> sanitize(c.getStringCellValue());
                case NUMERIC -> String.valueOf((int) c.getNumericCellValue());
                default -> "";
            };
            default -> "";
        };
    }
    
}
