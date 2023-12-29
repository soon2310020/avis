package com.stg.service3rd.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.stg.service3rd.common.utils.LogUtil.arrayToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDto {
    private String type;
    private String message;
    private String stackElements;

    public ExceptionDto(Exception ex) {
        this.type = ex.getClass().getName();
        this.message = ex.getMessage();
        this.stackElements = arrayToString(ex.getStackTrace());
    }

    @Override
    public String toString() {
        return "{type: '" + type + '\'' +
                ",\nmessage: '" + message + '\'' +
                ",\ntraces: " + stackElements +
                '}';
    }
}
