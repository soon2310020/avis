package com.stg.utils.excel;

public enum CellValueType {
    TEXT,   // text
    KEY_VALUE,  // value-label => chỉ lấy value
    FULL_DATE,  // dd/MM/yyyy HH:mm:ss
    SHORT_DATE,   // dd/MM/yyyy
    LONG_NUMBER,
    DOUBLE_NUMBER
}
