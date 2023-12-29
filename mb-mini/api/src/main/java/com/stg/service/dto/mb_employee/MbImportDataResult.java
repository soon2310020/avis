package com.stg.service.dto.mb_employee;

import com.stg.config.jackson.Jackson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.Future;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MbImportDataResult {
    private int totalRecord;
    private List<String> errorLines;
    List<Future<MbImportThreadResult>> futures;

    public String toStringErrorLines() {
        return Jackson.get().toJson(errorLines);
    }
}
