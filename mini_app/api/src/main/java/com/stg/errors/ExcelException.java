package com.stg.errors;

import lombok.Getter;

@Getter
public class ExcelException extends Exception{
    private String description;
    public ExcelException(String errorMessage){
        super(errorMessage);
    }
    public ExcelException(String errorMessage, String description){
        super(errorMessage);
        this.description = description;
    }
}
