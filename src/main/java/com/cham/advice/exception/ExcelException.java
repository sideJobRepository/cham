package com.cham.advice.exception;

public class ExcelException extends RuntimeException {
    private final int status;
    private final String fieldName;
    
    public ExcelException(String message, int status) {
        super(message);
        this.status = status;
        this.fieldName = null;
    }
    
    public ExcelException(String message, int status, String fieldName) {
        super(message);
        this.status = status;
        this.fieldName = fieldName;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}