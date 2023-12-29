package com.stg.service3rd.toolcrm.adapter;


import com.stg.service3rd.common.adapter.func.IFunction;

public enum ToolCrmFunctions implements IFunction {
    FIND_QUOTE_DETAIL("/v1/quotation/detail"),
    SYN_DATA("/v1/quotation/sync-data"),
    ;

    private final String uri;
    private final boolean hasSaveLog;

    ToolCrmFunctions(String uri) {
        this.uri = uri;
        this.hasSaveLog = true;
    }

    ToolCrmFunctions(String uri, boolean hasSaveLog) {
        this.uri = uri;
        this.hasSaveLog = hasSaveLog;
    }


    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public boolean hasSaveLog() {
        return hasSaveLog;
    }
}
