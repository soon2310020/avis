package com.stg.utils.excel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class CellInfo {
    private String key;
    private CellValueType cellType;
    private int maxLength;
    private Class classType;
    private boolean isAddress;
    private List<String> valueIn;
    private boolean required;

    public static CellInfo key(String key) {
        CellInfo cellInfo = new CellInfo();
        cellInfo.key = key;
        cellInfo.cellType = CellValueType.TEXT;
        cellInfo.classType = String.class;
        return cellInfo;
    }
    public static CellInfo key1(String key, String key2) {
        CellInfo cellInfo = new CellInfo();
        cellInfo.key = key;
        cellInfo.key = key2;
        cellInfo.cellType = CellValueType.TEXT;
        cellInfo.classType = String.class;
        return cellInfo;
    }


    public CellInfo text(int maxLength) {
        this.cellType = CellValueType.TEXT;
        this.maxLength = maxLength;
        this.classType = String.class;
        return this;
    }

    public CellInfo required() {
        this.setRequired(true);
        return this;
    }

    public CellInfo keyValue(int maxLength) {
        this.maxLength = maxLength;
        this.cellType = CellValueType.KEY_VALUE;
        this.classType = String.class;
        return this;
    }

    public CellInfo keyValue(int maxLength, List<String> valueIn) {
        this.maxLength = maxLength;
        this.cellType = CellValueType.KEY_VALUE;
        this.classType = String.class;
        this.valueIn = valueIn;
        return this;
    }

    public CellInfo longNumber() {
        this.maxLength = 15;
        this.cellType = CellValueType.LONG_NUMBER;
        this.classType = Long.class;
        return this;
    }

    public CellInfo doubleNumber() {
        this.maxLength = 15;
        this.cellType = CellValueType.DOUBLE_NUMBER;
        this.classType = Double.class;
        return this;
    }

    public CellInfo fullDate() {
        this.maxLength = 19;
        this.cellType = CellValueType.FULL_DATE;
        this.classType = Date.class;
        return this;
    }

    public CellInfo shortDate() {
        this.maxLength = 10;
        this.cellType = CellValueType.SHORT_DATE;
        this.classType = Date.class;
        return this;
    }

    public CellInfo address() {
        this.maxLength = 500;
        this.cellType = CellValueType.TEXT;
        this.classType = String.class;
        this.isAddress = true;
        return this;
    }
}