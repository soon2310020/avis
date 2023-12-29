package com.stg.service.dto.mb_employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MbImportThreadResult {
    private boolean success;
    private String errorDetail;
    private int total;

    public static MbImportThreadResult completed(int totalRecord) {
        return new MbImportThreadResult(true, null, totalRecord);
    }

    public static MbImportThreadResult failed(int totalRecord, String errorDetail) {
        return new MbImportThreadResult(false, errorDetail, totalRecord);
    }
}
